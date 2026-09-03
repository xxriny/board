# Spring Boot 게시판 REST API 아키텍처

## 목표

Java 17, Spring Boot 3, Spring Web MVC, Spring Data JPA, Hibernate 6, MySQL 8로 게시글과 1-depth 댓글을 관리하는 JSON REST API를 제공한다. 애플리케이션은 실행 가능한 JAR로 패키징하고 내장 Tomcat으로 실행한다.

## 기술 구성

- Java 17 (`sourceCompatibility`, `targetCompatibility` 모두 17)
- Spring Boot 3와 내장 Tomcat
- Spring Web MVC
- Spring Data JPA와 Hibernate 6
- Gradle `bootJar`
- Lombok, Jackson, Jakarta Validation
- springdoc OpenAPI 3와 `/swagger-ui/index.html` UI
- JSON 전용 HTTP 응답

v1의 순수 Spring MVC/WAR 구조는 `v1.0.0` 태그에서 보존한다. v2에서는 `web.xml`, 외부 Tomcat 배포, 수동 `RootConfig`/`WebConfig`를 사용하지 않는다.

## 런타임 구조

```text
board.jar
└── Spring Boot ApplicationContext
    ├── Embedded Tomcat
    ├── DispatcherServlet
    ├── Controller / GlobalExceptionHandler
    ├── Service / Transaction
    ├── Spring Data JPA Repository
    ├── DataSource / EntityManagerFactory / Hibernate
    └── springdoc OpenAPI / Swagger UI
```

## 설정 방식

`BoardApplication`이 `@SpringBootApplication`으로 컴포넌트 스캔과 자동 설정을 시작한다. Boot가 MVC, Jackson, Validation, DataSource, JPA, TransactionManager, 내장 Tomcat을 자동 구성한다.

`application.yml`은 프로젝트 루트의 `.env`를 선택적으로 불러오고 DB 접속 정보, JPA DDL 옵션, OSIV 비활성화, SQL 로그와 로그 레벨을 관리한다. 비밀번호 인코더는 `BCryptPasswordEncoder` 기반 `PasswordEncoder` Bean으로 등록한다.

로컬 기본 설정은 개발 편의를 위한 값이다. `ddl-auto=update`, SQL/DEBUG 로그, 파일 기반 `.env`는 운영 프로필에서 사용하지 않는다. 운영 환경은 별도 프로필로 스키마 검증과 마이그레이션 도구를 사용하고, 비밀값은 배포 환경의 secret 관리 수단으로 주입한다.

## 계층 책임

- Controller: URI 매핑, 입력 검증, HTTP 상태와 `ApiResponse<T>` 생성
- Service: 유스케이스, 리소스 존재 확인, 트랜잭션 경계, DTO 변환 조정
- Repository: Spring Data JPA 기본 CRUD, `Pageable` 페이징, 파생 쿼리
- Domain: 엔티티 불변식과 명시적 상태 변경
- DTO: 요청/응답 계약과 JPA 모델 격리
- Exception: 예외를 일관된 JSON 오류 응답으로 변환

Controller가 Repository를 직접 호출하거나 Repository가 DTO/HTTP 타입을 알도록 만들지 않는다.

데이터 접근 흐름은 `Service -> Spring Data JPA Repository -> EntityManager -> Hibernate -> MySQL`이다. EntityManager를 직접 호출하지 않아도 JPA 영속성 컨텍스트와 Hibernate 변경 감지는 그대로 사용한다.

## 트랜잭션

- 게시글 목록 및 댓글 목록은 `@Transactional(readOnly = true)`를 사용한다.
- 게시글 상세는 조회수를 증가시키므로 일반 `@Transactional`을 사용한다.
- 생성, 수정, 삭제는 일반 `@Transactional`을 사용한다.
- 엔티티 수정은 영속 상태에서 비즈니스 메서드를 호출하고 Hibernate 변경 감지로 반영한다.
- 댓글 생성·삭제는 같은 트랜잭션에서 `boards.comment_count`를 원자적 UPDATE로 증감한다. 별도 비동기 이벤트로 분리하지 않아 댓글 데이터와 집계 값의 커밋 원자성을 유지한다.

## OpenAPI 결정

v2에서는 springdoc Boot starter가 Controller의 OpenAPI 어노테이션을 읽어 `/v3/api-docs`를 생성한다. Swagger UI는 `/swagger-ui/index.html`에서 제공한다. 정적 `openapi.json`과 수동 `OpenApiController`는 사용하지 않는다.

## 보안 경계

- 게시글과 댓글 비밀번호는 BCrypt 해시로 저장하며 API 응답에 원문을 포함하지 않는다. 비밀번호 검증 실패 로그에도 원문을 기록하지 않는다.
- 현재 API는 계정 기반 인증, 권한 관리, 요청 횟수 제한을 제공하지 않는다. 리소스 비밀번호는 로컬 또는 신뢰된 환경의 간단한 수정·삭제 보호 수단이며, 인터넷 공개 서비스의 사용자 인증 수단으로 충분하지 않다.
- 외부에 API를 제공할 때는 TLS 종료를 구성하고, 프록시·방화벽에서 애플리케이션과 MySQL의 접근 대상을 제한한다. Swagger UI와 OpenAPI 엔드포인트도 공개 범위에 맞춰 제한한다.
- Spring Boot와 직접 지정한 라이브러리는 지원되는 보안 패치 버전으로 정기적으로 갱신한다.

## 테스트 경계

- 엔티티 테스트는 Spring 없이 실행한다.
- Service 테스트는 Repository를 대역으로 사용해 유스케이스와 예외를 검증한다.
- MVC 테스트는 `MockMvc` 또는 Boot 테스트 컨텍스트로 JSON 계약을 검증한다.
- Testcontainers MySQL 8 통합 테스트는 실제 Hibernate 매핑과 게시글 삭제 cascade를 검증한다.
- 최종 검증은 `clean test bootJar`와 Docker Compose 구성 검증으로 완료한다.
