# Spring MVC Board REST API Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a WAR-deployed JSON REST API for boards and one-depth comments using non-Boot Spring MVC and direct JPA EntityManager access.

**Architecture:** A DispatcherServlet child context owns MVC controllers, JSON, validation, and OpenAPI; a root context owns DataSource, JPA repositories, services, and transactions. Controllers return DTOs in `ApiResponse<T>`, services own transactions, and concrete repositories use JPQL through `EntityManager`.

**Tech Stack:** Java 17, Gradle WAR, Spring Framework 6.x, Hibernate 6.x, MySQL 8.0, HikariCP, Lombok, Jackson, Jakarta Validation, springdoc OpenAPI 3, JUnit 5, Mockito, AssertJ, MockMvc, Testcontainers, Tomcat 10.1.

**Spec:** `docs/architecture.md`, `docs/domain-model.md`, `docs/api-spec.md`, `docs/operations.md`, and `docs/board.erd`.

## Global Constraints

- Java 17 with `sourceCompatibility` and `targetCompatibility` set to 17.
- No Spring Boot, Spring Data JPA, `web.xml`, view templates, or `javax.*` APIs.
- Use `AbstractAnnotationConfigDispatcherServletInitializer` and Java Config.
- Use direct `EntityManager` access in concrete repository classes.
- Keep `@Transactional` in services and return DTOs rather than entities.
- Package a WAR for external Tomcat 10.1.
- Support only one-depth comments.
- Write a failing behavior test before each production implementation.

---

### Task 1: Gradle WAR and dependency boundary

**Files:**
- Create: `settings.gradle`
- Create: `build.gradle`
- Create: `gradle/wrapper/gradle-wrapper.properties`
- Create: `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`
- Delete: `src/Main.java`
- Test: `src/test/java/com/xxrin/board/architecture/DependencyRulesTest.java`

**Interfaces:**
- Produces: Java 17 WAR build, JUnit Platform test runtime, dependencies for later tasks.
- Enforces: no `org.springframework.boot`, no `org.springframework.data`, no `javax.*` production dependencies.

- [ ] **Step 1: Add a failing dependency rule test** that reads `build.gradle` and asserts the forbidden coordinates are absent and the `war` plugin plus Java 17 configuration are present.
- [ ] **Step 2: Run `./gradlew test --tests DependencyRulesTest`** and confirm failure because the Gradle project does not exist.
- [ ] **Step 3: Create the Gradle WAR project** with `mavenCentral()`, Java 17 toolchain/compatibility, JUnit Platform, Lombok annotation processing, Spring MVC/ORM/Test, Hibernate, MySQL driver, HikariCP, Jackson, Validation, Servlet API as `compileOnly`, OpenAPI, Mockito, AssertJ, and Testcontainers.
- [ ] **Step 4: Generate the Gradle wrapper** using a Java 17-compatible Gradle 8 release and remove the IntelliJ sample `src/Main.java`.
- [ ] **Step 5: Run `./gradlew dependencies`, `./gradlew test`, and `./gradlew war`**; confirm forbidden Boot/Data artifacts are absent and an empty WAR builds.
- [ ] **Step 6: Commit** with `build: scaffold Java 17 Spring MVC WAR project`.

### Task 2: Domain entities

**Files:**
- Create: `src/main/java/com/xxrin/board/domain/Board.java`
- Create: `src/main/java/com/xxrin/board/domain/Comment.java`
- Test: `src/test/java/com/xxrin/board/domain/BoardTest.java`
- Test: `src/test/java/com/xxrin/board/domain/CommentTest.java`

**Interfaces:**
- Produces: `Board.create/update/increaseViewCount/addComment/removeComment`, `Comment.create`, JPA mappings to `boards` and `comments`.
- Consumes: domain constraints from `docs/domain-model.md`.

- [ ] **Step 1: Write failing entity tests** for view count default/increment, title/content update, timestamps, and bidirectional Board–Comment association.
- [ ] **Step 2: Run `./gradlew test --tests '*domain.*'`** and confirm compilation fails because the entities are absent.
- [ ] **Step 3: Implement Board minimally** with Lombok getter/protected constructor/builder, identity ID, TEXT content, lifecycle timestamps, no setters, and explicit domain methods.
- [ ] **Step 4: Implement Comment minimally** with the required lazy Board association, no parent-comment field, lifecycle timestamp, and association-safe creation.
- [ ] **Step 5: Run the domain tests** and confirm all pass; inspect the classes to ensure no public setters or recursive Lombok methods exist.
- [ ] **Step 6: Commit** with `feat: add board and one-depth comment entities`.

### Task 3: Root JPA configuration and repositories

**Files:**
- Create: `src/main/resources/db.properties`
- Create: `src/main/java/com/xxrin/board/config/RootConfig.java`
- Create: `src/main/java/com/xxrin/board/repository/BoardRepository.java`
- Create: `src/main/java/com/xxrin/board/repository/CommentRepository.java`
- Create: `src/test/resources/db-test.properties`
- Test: `src/test/java/com/xxrin/board/repository/RepositoryIntegrationTest.java`

**Interfaces:**
- Produces: `BoardRepository.save/findById/findPage/count/delete`, `CommentRepository.save/findByBoardId/findByBoardIdAndId/delete`, JPA transaction manager.
- Consumes: `Board`, `Comment` from Task 2.

- [ ] **Step 1: Write a failing MySQL Testcontainers repository test** covering persist/find, `createdAt DESC, id DESC` board paging, count, `createdAt ASC, id ASC` comment ordering, and composite board/comment lookup.
- [ ] **Step 2: Run the repository test** and confirm failure because RootConfig and repositories are absent; if Docker is unavailable, record that environmental skip separately and continue with compilation tests.
- [ ] **Step 3: Implement RootConfig** with environment-aware HikariCP properties, `LocalContainerEntityManagerFactoryBean`, Hibernate scanning, vendor properties, `JpaTransactionManager`, transaction management, and exception translation.
- [ ] **Step 4: Implement both concrete repositories** with `@PersistenceContext EntityManager`, JPQL, `Optional`, `setFirstResult`, and `setMaxResults`; do not create repository interfaces.
- [ ] **Step 5: Run the integration test** and confirm mappings, queries, ordering, and count pass against MySQL 8.
- [ ] **Step 6: Commit** with `feat: configure JPA and EntityManager repositories`.

### Task 4: DTO contracts and common response

**Files:**
- Create: `src/main/java/com/xxrin/board/dto/request/BoardCreateRequest.java`
- Create: `src/main/java/com/xxrin/board/dto/request/BoardUpdateRequest.java`
- Create: `src/main/java/com/xxrin/board/dto/request/CommentCreateRequest.java`
- Create: `src/main/java/com/xxrin/board/dto/response/ApiResponse.java`
- Create: `src/main/java/com/xxrin/board/dto/response/PageResponse.java`
- Create: `src/main/java/com/xxrin/board/dto/response/BoardResponse.java`
- Create: `src/main/java/com/xxrin/board/dto/response/BoardDetailResponse.java`
- Create: `src/main/java/com/xxrin/board/dto/response/CommentResponse.java`
- Test: `src/test/java/com/xxrin/board/dto/DtoValidationTest.java`

**Interfaces:**
- Produces: immutable validated request records/classes and response factory methods `from(...)`, `ApiResponse.success/error`, `PageResponse.of(...)`.
- Consumes: domain entities and constraints in `docs/api-spec.md`.

- [ ] **Step 1: Write failing Validator tests** for blank and oversized title/writer/comment, optional content, and valid requests.
- [ ] **Step 2: Run the DTO tests** and confirm failure because DTO types are absent.
- [ ] **Step 3: Implement request DTOs** with exact Jakarta Validation annotations and Korean messages from the API contract.
- [ ] **Step 4: Implement response DTOs** as immutable values; map entities without exposing associations or persistence proxies.
- [ ] **Step 5: Run DTO tests** and add JSON serialization assertions for `success`, `data`, and `message`.
- [ ] **Step 6: Commit** with `feat: add validated API request and response DTOs`.

### Task 5: Services and transactional use cases

**Files:**
- Create: `src/main/java/com/xxrin/board/exception/EntityNotFoundException.java`
- Create: `src/main/java/com/xxrin/board/service/BoardService.java`
- Create: `src/main/java/com/xxrin/board/service/CommentService.java`
- Test: `src/test/java/com/xxrin/board/service/BoardServiceTest.java`
- Test: `src/test/java/com/xxrin/board/service/CommentServiceTest.java`

**Interfaces:**
- Produces: board CRUD/detail/page methods and comment create/list/delete methods returning response DTOs.
- Consumes: repository methods from Task 3 and DTO factories from Task 4.

- [ ] **Step 1: Write failing BoardService tests** for create, page metadata, detail view increment, update, delete, and every missing-board path.
- [ ] **Step 2: Write failing CommentService tests** for create/list/delete, missing Board, missing Comment, and comment ownership mismatch.
- [ ] **Step 3: Run service tests** and confirm failure because services are absent.
- [ ] **Step 4: Implement BoardService** with write transactions for mutation/detail and read-only transaction for paging; use domain methods and repository results.
- [ ] **Step 5: Implement CommentService** with write transactions for create/delete and read-only list; always validate Board existence and composite ownership.
- [ ] **Step 6: Run service tests** and inspect `@Transactional` placement to ensure repositories/controllers do not own use-case transactions.
- [ ] **Step 7: Commit** with `feat: implement transactional board and comment services`.

### Task 6: MVC, controllers, and exception JSON

**Files:**
- Create: `src/main/java/com/xxrin/board/config/WebAppInitializer.java`
- Create: `src/main/java/com/xxrin/board/config/WebConfig.java`
- Create: `src/main/java/com/xxrin/board/controller/BoardController.java`
- Create: `src/main/java/com/xxrin/board/controller/CommentController.java`
- Create: `src/main/java/com/xxrin/board/exception/GlobalExceptionHandler.java`
- Test: `src/test/java/com/xxrin/board/controller/BoardControllerTest.java`
- Test: `src/test/java/com/xxrin/board/controller/CommentControllerTest.java`
- Test: `src/test/java/com/xxrin/board/exception/GlobalExceptionHandlerTest.java`

**Interfaces:**
- Produces: all eight `/api/boards` endpoints, JSON-only conversion, validation and error status mapping.
- Consumes: service methods from Task 5 and common DTOs from Task 4.

- [ ] **Step 1: Write failing MockMvc tests** for all eight routes, status codes, wrapper fields, default/custom paging, and JSON content type.
- [ ] **Step 2: Add failing error tests** for validation maps, missing resources, malformed JSON, type mismatch, and sanitized 500 responses.
- [ ] **Step 3: Run MVC tests** and confirm failure because WebConfig/controllers/handler are absent.
- [ ] **Step 4: Implement WebAppInitializer and WebConfig** with separate root/servlet configs, `/` mapping, UTF-8 filter, Jackson, Validator, and method validation.
- [ ] **Step 5: Implement controllers** using `@RestController`, `@Valid`, bounded page/size validation, `ResponseEntity`, exact URI paths, and 201 for creates.
- [ ] **Step 6: Implement GlobalExceptionHandler** with deterministic field-error selection and the messages/statuses in `docs/api-spec.md`.
- [ ] **Step 7: Run all MVC/error tests** and confirm only JSON responses are produced.
- [ ] **Step 8: Commit** with `feat: expose validated JSON REST controllers`.

### Task 7: OpenAPI 3 and Swagger UI

**Files:**
- Modify: `build.gradle`
- Modify: `src/main/java/com/xxrin/board/config/WebConfig.java`
- Modify: `src/main/java/com/xxrin/board/controller/BoardController.java`
- Modify: `src/main/java/com/xxrin/board/controller/CommentController.java`
- Test: `src/test/java/com/xxrin/board/config/OpenApiIntegrationTest.java`

**Interfaces:**
- Produces: `/v3/api-docs`, `/swagger-ui/index.html`, Board/Comment tags and operation/response descriptions.
- Consumes: stable controller mappings from Task 6.

- [ ] **Step 1: Write failing integration tests** that request both documentation paths and assert OpenAPI version 3 plus Board and Comment tags.
- [ ] **Step 2: Run the OpenAPI test** and confirm 404 before configuration.
- [ ] **Step 3: Add the Spring 6-compatible springdoc WebMVC/UI modules** and explicit non-Boot Java Config; inspect `dependencies` to ensure the project does not become a Spring Boot application and does not introduce `SpringApplication`.
- [ ] **Step 4: Annotate controller classes and methods** with `@Tag`, `@Operation`, and documented 200/201/400/404 responses.
- [ ] **Step 5: Run OpenAPI tests and `./gradlew dependencies`**; confirm UI assets resolve under the WAR context and API JSON reflects all eight operations.
- [ ] **Step 6: Commit** with `docs: expose OpenAPI 3 and Swagger UI`.

### Task 8: Docker, environment, and cascade integration

**Files:**
- Create: `.env.example`
- Modify: `.gitignore`
- Create: `docker-compose.yml`
- Test: `src/test/java/com/xxrin/board/integration/BoardCommentIntegrationTest.java`

**Interfaces:**
- Produces: MySQL 8 service on port 3306, persistent volume, health check, environment contract.
- Consumes: JPA mapping and services from prior tasks.

- [ ] **Step 1: Write a failing integration test** that persists a Board with Comments, deletes the Board through the service, clears the persistence context, and asserts both comments are gone.
- [ ] **Step 2: Run the integration test** and confirm it fails before the final cascade/config behavior is verified.
- [ ] **Step 3: Create Docker Compose and environment templates** exactly as defined in `docs/operations.md`; ignore `.env`, build output, IDE-local state, and container data.
- [ ] **Step 4: Run `docker compose config`** and confirm all four required MySQL environment values, port mapping, health check, and named volume are present.
- [ ] **Step 5: Run the cascade integration test** against MySQL 8 and confirm orphan/cascade behavior after flush and clear.
- [ ] **Step 6: Commit** with `build: add MySQL Docker environment`.

### Task 9: README and end-to-end verification

**Files:**
- Create: `README.md`
- Modify: documentation only if verification finds a mismatch.

**Interfaces:**
- Produces: reproducible Docker → Gradle → Tomcat → Swagger → DataGrip instructions.
- Consumes: final paths, commands, environment keys, and HTTP contracts from all tasks.

- [ ] **Step 1: Write README** with prerequisites, directory layout, `.env` setup, MySQL startup/health, test/WAR commands, Tomcat 10.1 deployment, context-path-aware Swagger URLs, curl examples, and DataGrip values.
- [ ] **Step 2: Run `./gradlew clean test war`** and record the exact test count and WAR path from fresh output.
- [ ] **Step 3: Run `docker compose config` and `docker compose up -d mysql`**; confirm the service reaches healthy, then run the MySQL-backed integration tests.
- [ ] **Step 4: Deploy the fresh WAR to Tomcat 10.1** and smoke-test all eight API routes, `/v3/api-docs`, and `/swagger-ui/index.html`.
- [ ] **Step 5: Review every completion condition** in `docs/api-spec.md`, `docs/domain-model.md`, and `docs/operations.md`; correct any mismatch and rerun the affected verification.
- [ ] **Step 6: Commit** with `docs: add reproducible local run guide`.

## Definition of Done

- [ ] All eight APIs return the documented status and `ApiResponse<T>` JSON shape.
- [ ] Missing resources return 404 and invalid requests return field-specific 400 responses.
- [ ] Board detail increments view count and Board deletion cascades to Comments.
- [ ] No Spring Data JPA, Boot application bootstrap, `web.xml`, or view technology exists.
- [ ] `./gradlew clean test war` exits successfully.
- [ ] `docker compose config` is valid and MySQL-backed tests pass when Docker is available.
- [ ] Tomcat 10.1 serves the API and Swagger UI at the documented context path.
- [ ] README and DataGrip connection instructions reproduce the verified environment.
