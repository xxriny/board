# Spring MVC Board REST API Implementation Plan

> **Status (2026-09-02):** 최초 순수 JPA 구현을 완료한 뒤 요구사항 변경에 따라 Spring Data JPA Repository로 전환했다. 아래 작업별 체크박스는 최초 구현 계획의 기록이며, 현재 구조는 전환 설계서와 Git 커밋을 기준으로 판단한다.

**Goal:** Build a WAR-deployed JSON REST API for boards and one-depth comments using non-Boot Spring MVC and Spring Data JPA.

**Architecture:** Shared Spring MVC/JPA infrastructure is established first. API behavior is then implemented as complete vertical slices in CRUD order, with each slice adding its Repository, Service, Controller, DTO, and tests before the next behavior begins.

**Tech Stack:** Java 17, Gradle WAR, Spring Framework 6.x, Hibernate 6.x, MySQL 8.0, HikariCP, Lombok, Jackson, Jakarta Validation, OpenAPI 3, JUnit 5, Mockito, AssertJ, MockMvc, Testcontainers, Tomcat 10.1.

**Spec:** `docs/architecture.md`, `docs/domain-model.md`, `docs/api-spec.md`, `docs/operations.md`, and `docs/board.erd`.

## Global Constraints

- Java 17 with `sourceCompatibility` and `targetCompatibility` set to 17.
- No Spring Boot, `web.xml`, view templates, or `javax.*` APIs.
- Use `AbstractAnnotationConfigDispatcherServletInitializer` and Java Config.
- Use Spring Data JPA repositories configured through Java Config.
- Keep `@Transactional` in services and return DTOs rather than entities.
- Package a WAR for external Tomcat 10.1.
- Support only one-depth comments.
- Write a failing behavior test before each production implementation.
- After foundation work, execute Board C → R → U → D and then Comment C → R → U → D.
- Complete Repository → Service → Controller behavior inside one slice before moving to the next slice.
- Create one commit per functional slice; never group multiple CRUD behaviors or commit by technical layer.

---

### Task 1: Gradle WAR and dependency boundary

**Files:**
- Create: `settings.gradle`, `build.gradle`, and Gradle wrapper files
- Delete: `src/Main.java`
- Test: `src/test/java/com/xxrin/board/architecture/DependencyRulesTest.java`

**Interfaces:**
- Produces: Java 17 WAR build and JUnit Platform runtime.
- Enforces: no Spring Boot, Spring Data, or `javax.*` production dependency.

- [ ] Write a failing dependency rule test for Java 17, the `war` plugin, and forbidden coordinates.
- [ ] Run the test and confirm failure because the Gradle project is absent.
- [ ] Create the Gradle project with Spring MVC/ORM/Test, Hibernate, MySQL driver, HikariCP, Jackson, Jakarta Validation, Servlet API as `compileOnly`, Lombok, OpenAPI, JUnit, Mockito, AssertJ, and Testcontainers.
- [ ] Generate a Gradle 8 wrapper and remove the generated IntelliJ `src/Main.java`.
- [ ] Run `./gradlew dependencies`, `./gradlew test`, and `./gradlew war`.
- [ ] Commit with `feat: scaffold Java 17 Spring MVC WAR project`.

### Task 2: Shared domain and Spring foundation

**Files:**
- Create: `domain/Board.java`, `domain/Comment.java`
- Create: `config/RootConfig.java`, `config/WebConfig.java`, `config/WebAppInitializer.java`
- Create: `src/main/resources/db.properties`
- Create: `dto/response/ApiResponse.java`
- Create: `exception/EntityNotFoundException.java`, `exception/GlobalExceptionHandler.java`
- Test: domain tests and `config/ApplicationContextTest.java`

**Interfaces:**
- Produces: JPA entities, Spring contexts, transaction manager, JSON/validation, common response, and shared exception handling.

- [ ] Write failing entity tests for view count, update, timestamps, and Board–Comment association.
- [ ] Implement Board and Comment with physical mappings, protected constructors, builders, and no setters or parent-comment field.
- [ ] Run domain tests and commit with `feat: add board and comment domain model`.
- [ ] Write a failing context test for root/servlet contexts, JPA, transactions, Jackson, and Validator.
- [ ] Implement RootConfig, WebConfig, and WebAppInitializer without Boot or `web.xml`.
- [ ] Implement `ApiResponse.success/error` and baseline 400/404/500 mappings.
- [ ] Run context tests and `./gradlew war`.
- [ ] Commit with `feat: add Spring MVC JPA application foundation`.

### Task 3: Board Create slice

**Files:**
- Create: `dto/request/BoardCreateRequest.java`, `dto/response/BoardResponse.java`
- Create: `repository/BoardRepository.java`, `service/BoardService.java`, `controller/BoardController.java`
- Test: `src/test/java/com/xxrin/board/board/BoardCreateTest.java`

**Interfaces:**
- Produces: `BoardRepository.save`, `BoardService.create`, and `POST /api/boards` with 201.

- [ ] Write failing validation tests for title, content, writer, and password constraints.
- [ ] Write a failing repository test proving persist assigns ID and timestamps.
- [ ] Write a failing service test for creation and response mapping.
- [ ] Write failing MockMvc tests for 201 and field-specific 400 JSON.
- [ ] Implement DTOs, repository save, transactional service create, and POST controller method.
- [ ] Run the slice test and full suite.
- [ ] Commit with `feat: add board creation API`.

### Task 4: Board Read slices

**Files:**
- Create: `dto/response/PageResponse.java`, `dto/response/BoardDetailResponse.java`
- Modify: Board Repository, Service, and Controller
- Test: `src/test/java/com/xxrin/board/board/BoardReadTest.java`

**Interfaces:**
- Produces two independent behaviors: paged `GET /api/boards` and detail `GET /api/boards/{id}`.

- [ ] Write failing repository/service/MockMvc tests for `createdAt DESC, id DESC` paging, count, defaults, bounds, and page metadata.
- [ ] Implement `PageResponse`, JPQL paging with `setFirstResult`/`setMaxResults`, count, read-only list service, and list controller.
- [ ] Run Board list tests and the full suite.
- [ ] Commit only the list behavior with `feat: add board list API`.
- [ ] Write failing repository/service/MockMvc tests for ID lookup, missing Board, comments, and view-count increment.
- [ ] Implement `BoardDetailResponse`, ID lookup, write-transaction detail service, and detail controller.
- [ ] Run Board detail tests and the full suite.
- [ ] Commit only the detail behavior with `feat: add board detail API`.

### Task 5: Board Update slice

**Files:**
- Create: `dto/request/BoardUpdateRequest.java`
- Modify: Board Service and Controller
- Test: `src/test/java/com/xxrin/board/board/BoardUpdateTest.java`

**Interfaces:**
- Produces: password-protected `BoardService.update` and `PUT /api/boards/{id}`.

- [ ] Write failing validation, service, and MockMvc tests for 200, 400, 403, and 404.
- [ ] Assert writer, ID, createdAt, and viewCount remain unchanged.
- [ ] Implement update DTO, dirty-checking service method, and PUT controller method.
- [ ] Run the slice test and full suite.
- [ ] Commit with `feat: add board update API`.

### Task 6: Board Delete slice

**Files:**
- Modify: Board Repository, Service, and Controller
- Test: `src/test/java/com/xxrin/board/board/BoardDeleteTest.java`

**Interfaces:**
- Produces: repository remove, password verification, service delete, and `DELETE /api/boards/{id}?password={password}`.

- [ ] Write a failing MySQL test that deletes a Board and verifies its Comments are gone after flush/clear.
- [ ] Write failing service/controller tests for success, password mismatch 403, and missing Board 404.
- [ ] Implement repository remove, transactional delete, and DELETE controller method.
- [ ] Run the slice test and full suite.
- [ ] Commit with `feat: add board deletion API`.

### Task 7: Comment Create slice

**Files:**
- Create: `dto/request/CommentCreateRequest.java`, `dto/response/CommentResponse.java`
- Create: `repository/CommentRepository.java`, `service/CommentService.java`, `controller/CommentController.java`
- Test: `src/test/java/com/xxrin/board/comment/CommentCreateTest.java`

**Interfaces:**
- Produces: BCrypt 비밀번호 해시를 저장하는 comment save와 `POST /api/boards/{boardId}/comments` with 201.

- [ ] Write failing validation, repository, service, and MockMvc tests for success, invalid input, and missing Board.
- [ ] Implement DTOs, concrete EntityManager repository, transactional service, and POST controller.
- [ ] Assert the Comment references Board and has no parent-comment field.
- [ ] Run the slice test and full suite.
- [ ] Commit with `feat: add comment creation API`.

### Task 8: Comment Read slice

**Files:**
- Modify: Comment Repository, Service, and Controller
- Test: `src/test/java/com/xxrin/board/comment/CommentReadTest.java`

**Interfaces:**
- Produces: ordered list query and `GET /api/boards/{boardId}/comments`.

- [ ] Write a failing repository test for `createdAt ASC, id ASC` ordering scoped to one Board.
- [ ] Write failing service/controller tests for list, empty list, and missing Board 404.
- [ ] Implement JPQL lookup, Board validation, DTO mapping, and GET controller.
- [ ] Run the slice test and full suite.
- [ ] Commit with `feat: add comment read API`.

### Task 9: Comment Update slice

**Files:**
- Create: `dto/request/CommentUpdateRequest.java`
- Modify: Comment Repository, Service, and Controller
- Test: `src/test/java/com/xxrin/board/comment/CommentUpdateTest.java`

**Interfaces:**
- Produces: 비밀번호 검증 후 content-only update와 `PUT /api/boards/{boardId}/comments/{commentId}`.

- [ ] Write failing tests for success, invalid content, missing Comment, and ownership mismatch.
- [ ] Implement update request, composite lookup, transactional dirty-checking update, and nested PUT controller.
- [ ] Assert writer, Board, and createdAt remain unchanged while updatedAt advances.
- [ ] Run the slice test and full suite.
- [ ] Commit with `feat: add comment update API`.

### Task 10: Comment Delete slice

**Files:**
- Modify: Comment Repository, Service, and Controller
- Test: `src/test/java/com/xxrin/board/comment/CommentDeleteTest.java`

**Interfaces:**
- Produces: 비밀번호 검증을 포함한 composite lookup/remove와 `DELETE /api/boards/{boardId}/comments/{commentId}?password={password}`.

- [ ] Write failing tests for success, missing Board, missing Comment, and ownership mismatch.
- [ ] Implement `findByBoardIdAndId`, remove, transactional service delete, and nested DELETE controller.
- [ ] Return the same 404 for a missing Comment and ownership mismatch.
- [ ] Run the slice test and full suite.
- [ ] Commit with `feat: add comment deletion API`.

### Task 11: OpenAPI, Docker, and delivery verification

**Files:**
- Modify: build/config/controllers and `.gitignore`
- Create: `.env.example`, `docker-compose.yml`, `README.md`
- Test: `config/OpenApiIntegrationTest.java`

**Interfaces:**
- Produces: OpenAPI paths/tags, Swagger UI, MySQL Compose service, and run guide.

- [ ] Write failing tests for OpenAPI 3, all nine operations, Board/Comment tags, and Swagger UI.
- [ ] Add Swagger Core Jakarta annotations, static OpenAPI JSON, Swagger UI WebJar, and non-Boot Spring MVC resource handling.
- [ ] Run OpenAPI tests and the full suite, then commit only this capability with `feat: add OpenAPI documentation endpoints`.
- [ ] Create MySQL 8 Compose with port 3306, `board_db`, health check, named volume, and `.env.example`.
- [ ] Run `docker compose config` and MySQL-backed tests, then commit only runtime configuration with `feat: add MySQL Docker runtime`.
- [ ] Write README covering Docker, Gradle WAR, Tomcat, Swagger, curl, and DataGrip.
- [ ] Verify every documented command, then commit only the guide with `docs: add local development guide`.
- [ ] Run final `./gradlew clean test war` and Tomcat smoke tests without folding unrelated fixes into one commit.

## Definition of Done

- [x] CRUD slices were completed in Board C → R → U → D, then Comment C → R → U → D order.
- [x] All nine APIs return the documented status and `ApiResponse<T>` JSON shape; Board와 Comment 수정·삭제는 BCrypt 비밀번호 검증을 거친다.
- [x] Missing resources return 404 and invalid requests return field-specific 400 responses.
- [x] Board detail increments view count and Board deletion cascades to Comments.
- [x] Spring Data JPA runs through Java Config without Boot bootstrap, `web.xml`, or view technology.
- [x] `./gradlew clean test war` exits successfully.
- [x] Docker Compose and MySQL-backed tests pass when Docker is available.
- [x] Tomcat 10.1 serves the API and Swagger UI at the `/board` context path.
