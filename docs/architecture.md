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

`application.properties`는 DB 접속 정보, JPA DDL 옵션, SQL 로그, JSON 날짜 직렬화, 로그 레벨을 관리한다. 비밀번호 인코더는 `PasswordEncoder` Bean으로 등록한다.

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

## OpenAPI 결정

v2에서는 springdoc Boot starter가 Controller의 OpenAPI 어노테이션을 읽어 `/v3/api-docs`를 생성한다. Swagger UI는 `/swagger-ui/index.html`에서 제공한다. 정적 `openapi.json`과 수동 `OpenApiController`는 사용하지 않는다.

## 테스트 경계

- 엔티티 테스트는 Spring 없이 실행한다.
- Service 테스트는 Repository를 대역으로 사용해 유스케이스와 예외를 검증한다.
- MVC 테스트는 `MockMvc` 또는 Boot 테스트 컨텍스트로 JSON 계약을 검증한다.
- Testcontainers MySQL 8 통합 테스트는 실제 Hibernate 매핑과 게시글 삭제 cascade를 검증한다.
- 최종 검증은 `clean test bootJar`와 Docker Compose 구성 검증으로 완료한다.
