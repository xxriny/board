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
- Use plain JPA with an injected `EntityManager`; do not add Spring Data JPA.
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

## Verification

Run the following before reporting completion:

```bash
./gradlew clean test war
docker compose config
```

When Docker is available, also run the Testcontainers integration suite and verify the MySQL service health check.
