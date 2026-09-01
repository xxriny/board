# Spring MVC 게시판 REST API 아키텍처

## 목표

Spring Boot와 Spring Data JPA 없이 Java 17, Spring MVC 6, Hibernate 6, MySQL 8로 게시글과 1-depth 댓글을 관리하는 JSON REST API를 제공한다. 애플리케이션은 WAR로 패키징하여 외부 Tomcat 10.1에 배포한다.

## 기술 제약

- Java 17 (`sourceCompatibility`, `targetCompatibility` 모두 17)
- Spring Framework 6.x와 Jakarta Servlet API
- Java Config 및 `AbstractAnnotationConfigDispatcherServletInitializer`
- Gradle `war` 플러그인
- Hibernate 6 기반 순수 JPA와 직접 주입한 `EntityManager`
- HikariCP DataSource와 `JpaTransactionManager`
- Lombok, Jackson, Jakarta Validation
- OpenAPI 3 문서와 `/swagger-ui/index.html` UI
- JSON 전용 HTTP 응답

Spring Boot, Spring Data JPA, `web.xml`, JSP, Thymeleaf 및 `javax.*` API는 사용하지 않는다.

## 런타임 구조

```text
Tomcat 10.1
└── board.war
    ├── Root ApplicationContext
    │   ├── DataSource
    │   ├── EntityManagerFactory
    │   ├── JpaTransactionManager
    │   ├── Repository
    │   └── Service
    └── DispatcherServlet ApplicationContext
        ├── Controller
        ├── GlobalExceptionHandler
        ├── Jackson / Validation
        └── OpenAPI / Swagger UI
```

## 설정 계층

### WebAppInitializer

`AbstractAnnotationConfigDispatcherServletInitializer`를 확장한다. Root 컨텍스트에는 `RootConfig`, Servlet 컨텍스트에는 `WebConfig`를 등록하고 DispatcherServlet을 `/`에 매핑한다. UTF-8 `CharacterEncodingFilter`를 추가하며 `web.xml`은 만들지 않는다.

### RootConfig

`service`와 `repository` 패키지를 스캔한다. `db.properties`와 환경변수에서 DB 구성을 읽고 HikariCP DataSource, `LocalContainerEntityManagerFactoryBean`, Hibernate vendor adapter, `JpaTransactionManager`를 등록한다. `@EnableTransactionManagement`와 `PersistenceExceptionTranslationPostProcessor`를 활성화한다.

### WebConfig

`@EnableWebMvc`와 `WebMvcConfigurer`를 사용하며 `controller`와 `exception` 패키지를 스캔한다. Jackson JSON 변환기, Jakarta Validator, 메서드 검증 및 Swagger UI 정적 리소스를 등록한다.

## 계층 책임

- Controller: URI 매핑, 입력 검증, HTTP 상태와 `ApiResponse<T>` 생성
- Service: 유스케이스, 리소스 존재 확인, 트랜잭션 경계, DTO 변환 조정
- Repository: JPQL, 페이징, `EntityManager` 영속화 및 조회
- Domain: 엔티티 불변식과 명시적 상태 변경
- DTO: 요청/응답 계약과 JPA 모델 격리
- Exception: 예외를 일관된 JSON 오류 응답으로 변환

Controller가 Repository를 직접 호출하거나 Repository가 DTO/HTTP 타입을 알도록 만들지 않는다.

## 트랜잭션

- 게시글 목록 및 댓글 목록은 `@Transactional(readOnly = true)`를 사용한다.
- 게시글 상세는 조회수를 증가시키므로 일반 `@Transactional`을 사용한다.
- 생성, 수정, 삭제는 일반 `@Transactional`을 사용한다.
- 엔티티 수정은 영속 상태에서 비즈니스 메서드를 호출하고 Hibernate 변경 감지로 반영한다.

## OpenAPI 결정

Spring Boot 의존성을 피하기 위해 springdoc starter 대신 Swagger Core Jakarta 어노테이션과 Swagger UI WebJar를 사용한다. `openapi.json`을 OpenAPI 3 계약의 기준으로 관리하고 `OpenApiController`가 `/v3/api-docs`와 `/swagger-ui/index.html`을 제공한다. `WebConfig`는 WebJar 정적 리소스와 JSON, 문자열, Resource 응답 컨버터를 명시적으로 등록한다. Springfox 3은 Jakarta 기반 Spring 6과 맞지 않으므로 사용하지 않는다.

## 테스트 경계

- 엔티티 테스트는 Spring 없이 실행한다.
- Service 테스트는 Repository를 대역으로 사용해 유스케이스와 예외를 검증한다.
- MVC 테스트는 `MockMvc` standalone 또는 명시적 WebConfig 컨텍스트로 JSON 계약을 검증한다.
- Testcontainers MySQL 8 통합 테스트는 실제 Hibernate 매핑과 게시글 삭제 cascade를 검증한다.
- 최종 검증은 `clean test war`와 Docker Compose 구성 검증으로 완료한다.
