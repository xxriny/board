# Board API Agent Guide

## Mission

Build and maintain the non-Boot Spring MVC board REST API described in `docs/`.

## Read Order

Before changing production code, read these files in order:

1. `docs/README.md`
2. `docs/architecture.md`
3. `docs/domain-model.md`
4. `docs/api-spec.md`
5. `docs/operations.md`
6. `docs/plan.md`

`docs/board.erd` is the ERD Editor source for the logical and physical database model.

## Non-negotiable Constraints

- Use Java 17 and Spring Framework 6.x without Spring Boot.
- Register Spring MVC through `AbstractAnnotationConfigDispatcherServletInitializer`; never add `web.xml`.
- Package the application as a WAR for Tomcat 10.1.
- Use Jakarta APIs only; do not introduce `javax.*` imports.
- Use Spring Data JPA repositories configured through Java Config without Spring Boot.
- Prefer inherited CRUD, `Pageable`, and derived queries; add custom `EntityManager` code only when a query cannot be expressed clearly.
- Keep transaction boundaries in the service layer.
- Return DTOs wrapped in `ApiResponse<T>`; never serialize JPA entities directly.
- Keep entities free of public setters. Mutate state through domain methods.
- Keep comments at one depth; never add a parent-comment association.
- Keep database credentials outside tracked files.
- Add or change behavior through a failing test first, then implement the minimum code to pass it.

## Source Layout

- Production Java: `src/main/java/com/xxrin/board`
- Resources: `src/main/resources`
- Web resources: `src/main/webapp`
- Tests: `src/test/java/com/xxrin/board`

Delete the generated IntelliJ sample `src/Main.java` when Gradle scaffolding is introduced; it is not part of the application.

## Development Order

After the shared Gradle, Spring, JPA, domain, DTO wrapper, validation, and exception foundations are ready, implement behavior as vertical API slices in this order:

1. Board Create
2. Board Read: list, then detail with view-count increment
3. Board Update
4. Board Delete
5. Comment Create
6. Comment Read
7. Comment Update
8. Comment Delete

For each slice, follow RED → GREEN → REFACTOR across Repository, Service, Controller, and tests before starting the next slice. Do not implement all repositories, then all services, then all controllers.

## Verification

Run the following before reporting completion:

```bash
./gradlew clean test war
docker compose config
```

When Docker is available, also run the Testcontainers integration suite and verify the MySQL service health check.

## Commit Convention

Use the following format:

```text
<type>: <summary>
```

Only these commit types are allowed:

- `feat`: production code, configuration, or a user-visible capability
- `docs`: documentation-only changes, including plans and ERD updates
- `test`: test-only changes with no production behavior change

Commit by functional vertical slice, not by technical layer:

- One API behavior or one shared foundation capability per commit.
- Include the Repository, Service, Controller, DTO, and tests for that behavior in the same `feat` commit.
- Never combine Create, Read, Update, or Delete behaviors in one commit.
- Split Board list and Board detail into separate Read commits because they have different queries and transaction behavior.
- Keep OpenAPI, Docker runtime configuration, and README documentation in separate commits.
- Do not create layer-only commits such as "add repositories" or "add controllers".

Keep the type lowercase, write a concise imperative summary, and do not end the subject with a period. Each commit must contain one logical change.

When a feature includes its tests, use `feat`. Use `test` only when adding or correcting tests without changing production code. Keep unrelated documentation in a separate `docs` commit.

Examples:

```text
feat: add board creation endpoint
docs: document local Tomcat deployment
test: cover comment ownership validation
```
