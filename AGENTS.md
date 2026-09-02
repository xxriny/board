# Board API 에이전트 가이드

## 목표

`docs/`에 정리된 게시판 REST API를 구현하고 유지보수한다. v1은 Spring MVC/WAR 버전으로 `v1.0.0` 태그에 보존하고, 현재 작업은 Spring Boot 기반 v2를 기준으로 진행한다.

## 문서 읽기 순서

프로덕션 코드를 변경하기 전에 다음 문서를 순서대로 확인한다.

1. `docs/README.md`
2. `docs/architecture.md`
3. `docs/domain-model.md`
4. `docs/api-spec.md`
5. `docs/operations.md`
6. `docs/plan.md`

`docs/board.erd`는 논리·물리 데이터베이스 모델의 ERD Editor 원본이다.

## 핵심 제약

- Java 17과 Spring Boot 3을 사용한다.
- v2는 내장 Tomcat 실행 가능한 `bootJar`를 기본 산출물로 사용한다.
- Jakarta API만 사용하고 `javax.*` import를 추가하지 않는다.
- Spring Data JPA Repository를 사용한다.
- 단순 CRUD, 페이징, 단순 조건 조회는 Spring Data JPA 기본 메서드·파생 쿼리·`Pageable`을 우선 사용한다.
- 복잡한 동적 쿼리나 통계성 쿼리가 필요할 때만 `@Query`, Custom Repository, `EntityManager` 직접 구현을 고려한다.
- 트랜잭션 경계는 Service 계층에 둔다.
- 외부 응답은 `ApiResponse<T>`로 감싼 DTO를 반환하며 JPA Entity를 직접 직렬화하지 않는다.
- Entity에는 public Setter를 두지 않고 도메인 메서드로 상태를 변경한다.
- 댓글은 1-depth만 지원하며 부모 댓글 연관관계를 추가하지 않는다.
- 게시글과 댓글 비밀번호 원문은 저장하지 않고 해시로 저장한다.
- DB 계정, 비밀번호, 개인 토큰 등 비밀값은 추적 파일에 저장하지 않는다.

## 소스 구조

- 프로덕션 Java: `src/main/java/com/xxrin/board`
- 리소스: `src/main/resources`
- 테스트: `src/test/java/com/xxrin/board`

Gradle 스캐폴딩 후 생성된 IntelliJ 샘플 `src/Main.java`는 애플리케이션 코드가 아니므로 유지하지 않는다.

## 작업 위치

작업은 기본적으로 메인 프로젝트 경로인 `/Users/xxrin/dev/board`에서 진행한다. 별도 지시가 없는 한 `.worktrees/` 아래의 격리 worktree에서 작업하지 않는다.

## 개발 순서

공통 Gradle, Spring Boot, JPA, 도메인, DTO 래퍼, 검증, 예외 처리 기반을 준비한 뒤 API 동작은 세로 기능 단위로 구현한다.

1. Board Create
2. Board Read: 목록 조회, 상세 조회와 조회수 증가
3. Board Update
4. Board Delete
5. Comment Create
6. Comment Read
7. Comment Update
8. Comment Delete

각 기능은 Repository, Service, Controller, DTO, 테스트가 함께 동작하는 단위로 완성한다. Repository만 먼저 만들고 Service·Controller를 나중에 몰아서 만드는 식의 계층별 작업은 피한다.

중요 동작이나 비즈니스 로직이 큰 부분은 TDD로 진행한다. 예를 들어 비밀번호 검증, 댓글 소속 검증, 조회수 증가, cascade 삭제, 트랜잭션 경계처럼 동작 실패 시 영향이 큰 부분은 실패 테스트를 먼저 작성한 뒤 구현한다.

단순 문서 수정, 설정 정리, 명확한 기계적 변경, 작은 DTO 필드 추가처럼 위험이 낮은 작업은 TDD를 강제하지 않는다. 이 경우에도 최소한의 단위 테스트나 기존 테스트 실행으로 변경 영향을 확인한다.

## 검증

완료를 보고하기 전에 작업 범위에 맞는 검증을 실행한다.

```bash
./gradlew clean test bootJar
docker compose config
```

Docker를 사용할 수 있으면 Testcontainers 기반 MySQL 통합 테스트와 MySQL health check도 확인한다. Docker daemon이 꺼져 있거나 환경 문제로 실행할 수 없으면, Gradle 검증 결과와 Docker 미검증 사유를 분리해서 보고한다.

## 커밋 규칙

커밋 메시지는 다음 형식을 사용한다.

```text
<type>: <한글 요약>
```

허용되는 커밋 type은 다음 여섯 가지뿐이다.

- `feat`: 프로덕션 코드, 설정, 사용자에게 보이는 기능 변경
- `fix`: 버그 수정 또는 의도와 다르게 동작하던 기능 수정
- `refactor`: 외부 동작 변경 없이 코드 구조 개선
- `docs`: 문서만 변경
- `test`: 프로덕션 동작 변경 없이 테스트만 변경
- `chore`: 빌드, 의존성, 도구 설정, 포맷 등 기능 동작과 직접 관련 없는 관리 작업

커밋 제목, 본문, 상세 설명은 한글로 작성한다. type은 Conventional Commit 관례에 맞춰 영문 소문자로 작성한다.

커밋은 기능 단위로 쪼갠다.

- 하나의 API 동작 또는 하나의 공통 기반 기능을 하나의 커밋으로 묶는다.
- Create, Read, Update, Delete 동작을 한 커밋에 섞지 않는다.
- OpenAPI, Docker 실행 환경, README 문서는 각각 별도 커밋으로 관리한다.
- `repository 추가`, `controller 추가`처럼 계층만 기준으로 나눈 커밋은 만들지 않는다.
- 개인 계획 문서나 커밋하지 않기로 한 파일은 커밋에 포함하지 않는다.

기능 구현과 그 기능을 검증하는 테스트가 함께 들어가면 `feat`를 사용한다. 버그 수정과 그 검증 테스트가 함께 들어가면 `fix`를 사용한다. 외부 동작을 바꾸지 않는 구조 개선은 `refactor`를 사용한다. 프로덕션 코드 변경 없이 테스트만 추가하거나 수정하면 `test`를 사용한다. 관련 없는 문서 변경은 별도 `docs` 커밋으로 분리한다.

예시:

```text
feat: 게시글 생성 API 추가
fix: 댓글 수정 비밀번호 검증 누락 수정
refactor: 게시글 조회 응답 변환 로직 분리
docs: 로컬 실행 방법 정리
test: 댓글 소속 검증 테스트 추가
chore: Gradle 의존성 버전 정리
```
