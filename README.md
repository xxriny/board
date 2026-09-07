# Spring Boot Board REST API

Java 17과 Spring Boot 3으로 구현한 회원 기반 게시판 REST API다. JWT 인증, 회원 소유권 기반 게시글·1-depth 댓글 CRUD, 공통 JSON 응답과 OpenAPI 문서를 제공하며 내장 Tomcat으로 실행한다.

v1 순수 Spring MVC/WAR 버전은 `v1.0.0` 태그에 보존되어 있다.

## 기술 구성

- Java 17, Gradle, Spring Boot 3.5
- Spring Web MVC, Spring Data JPA, Hibernate 6
- Spring Security OAuth2 Resource Server, HS256 JWT
- MySQL 8.0, Docker Compose, Testcontainers
- Jackson, Jakarta Validation, Lombok
- springdoc OpenAPI 3, Swagger UI

## v1에서 v2로 달라진 점

| 구분 | v1 (`v1.0.0`) | 현재 v2 |
| --- | --- | --- |
| 실행 | 외부 Tomcat에 WAR 배포 | 내장 Tomcat과 실행 가능한 `board.jar` |
| 설정 | 수동 Spring MVC/JPA 설정 | Spring Boot 자동 설정과 `application.yml` |
| 데이터 접근 | 직접 구현한 `EntityManager` Repository | Spring Data JPA Repository |
| 작성자 인증 | 요청의 writer와 게시글·댓글 비밀번호 | 회원 계정과 JWT |
| 권한 확인 | 리소스 비밀번호 비교 | JWT 회원과 작성자 소유권 비교 |
| API 응답 | `ApiResponse<T>` | `ApiResult<T>` |
| OpenAPI | 정적 명세와 수동 제공 | Controller 애너테이션에서 springdoc이 생성 |

### 추가된 것

- 회원가입, 로그인, 토큰 재발급, 현재·전체 기기 로그아웃
- 내 회원 정보 조회·수정
- `members`, `refresh_tokens` 테이블과 회원 FK
- 15분 Access Token과 14일 Refresh Token 회전
- 게시글·댓글 작성자 소유권 검사
- `ErrorCode`, `BusinessException`, `GlobalExceptionHandler` 기반 공통 오류 처리
- 댓글 수 물리 컬럼의 원자적 증가·감소
- OpenAPI summary·description 검증과 Testcontainers MySQL 통합 테스트

### 사라지거나 대체된 것

- 외부 Tomcat 배포와 WAR 산출물
- 게시글·댓글 요청의 `writer`, `password`
- 게시글·댓글의 `password_hash` 컬럼
- 수동 `RootConfig`, `WebConfig`, `PersistenceConfig`
- 정적 `openapi.json`, 수동 `OpenApiController`
- `PasswordRequest`, `EntityNotFoundException`, `InvalidPasswordException`
- 이름이 겹치던 커스텀 `ApiResponse` 클래스

## 1. 환경변수 준비

```bash
cp .env.example .env
openssl rand -base64 32
```

생성된 값을 `.env`의 `AUTH_JWT_SECRET`에 넣고 DB 비밀번호도 로컬 값으로 변경한다. 실제 `.env`는 Git에서 제외되며 운영 비밀값은 배포 환경의 secret 관리 수단으로 주입한다.

## 2. MySQL 실행

```bash
docker compose up -d mysql
docker compose ps
```

`board-mysql` 상태가 `healthy`가 되면 애플리케이션에서 접속할 수 있다. Compose의 기본 비밀번호와 포트 공개 방식은 로컬 개발 전용이다.

## 3. 테스트와 빌드

```bash
./gradlew clean test bootJar
docker compose config
```

실행 파일은 `build/libs/board.jar`에 생성된다.

## 4. 애플리케이션 실행

```bash
./gradlew bootRun
```

또는 빌드된 JAR를 실행한다.

```bash
java -jar build/libs/board.jar
```

두 명령은 프로젝트 루트의 `.env`를 읽으므로 프로젝트 루트에서 실행한다.

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- 게시글 API: `http://localhost:8080/api/boards`

## API 빠른 확인

`curl`과 `jq`가 필요하다.

### 회원가입

```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "user@example.com",
    "password": "Board!1234",
    "nickname": "사용자",
    "phone": "01012345678"
  }'
```

### 로그인

로그인 응답의 Access Token을 셸 변수에 저장하고 Refresh Token 쿠키는 임시 파일에 저장한다.

```bash
ACCESS_TOKEN=$(curl -sS -c /tmp/board-refresh-cookie.txt \
  -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"user@example.com","password":"Board!1234"}' \
  | jq -r '.data.accessToken')
```

### 게시글과 댓글

조회는 공개지만 생성·수정·삭제에는 Access Token이 필요하다.

```bash
curl -X POST http://localhost:8080/api/boards \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"title":"첫 글","content":"내용"}'

curl 'http://localhost:8080/api/boards?page=0&size=10'
curl http://localhost:8080/api/boards/1

curl -X PUT http://localhost:8080/api/boards/1 \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"title":"수정 제목","content":"수정 내용"}'

curl -X POST http://localhost:8080/api/boards/1/comments \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"content":"댓글"}'

curl http://localhost:8080/api/boards/1/comments

curl -X DELETE http://localhost:8080/api/boards/1/comments/1 \
  -H "Authorization: Bearer $ACCESS_TOKEN"

curl -X DELETE http://localhost:8080/api/boards/1 \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

### 토큰 재발급

사용한 Refresh Token은 폐기되고 새 쿠키로 교체된다.

```bash
curl -b /tmp/board-refresh-cookie.txt -c /tmp/board-refresh-cookie.txt \
  -X POST http://localhost:8080/api/auth/refresh
```

전체 엔드포인트와 오류 코드는 [API 명세](docs/api-spec.md)에서 확인한다.

## 공통 응답

성공과 오류는 모두 `ApiResult<T>`로 반환한다.

```json
{
  "success": true,
  "data": {},
  "message": "요청이 성공했습니다."
}
```

```json
{
  "success": false,
  "data": null,
  "code": "BOARD_NOT_FOUND",
  "message": "게시글을 찾을 수 없습니다."
}
```

Security 필터에서 발생한 401·403도 같은 형식으로 반환한다.

## 데이터 모델

```text
members 1 ─── 0..N boards
members 1 ─── 0..N comments
members 1 ─── 0..N refresh_tokens
boards  1 ─── 0..N comments
```

회원 비밀번호는 BCrypt 해시로 저장하고 Refresh Token 원문은 저장하지 않는다. 게시글 목록과 상세 응답의 `commentCount`는 `boards.comment_count` 컬럼에 저장하며 댓글 생성·삭제 트랜잭션에서 원자적으로 증감한다.

저장소의 ERD 원본은 [`docs/board.erd`](docs/board.erd), 상세 규칙은 [도메인 모델 문서](docs/domain-model.md)에서 확인한다.

## 테스트 데이터 스크립트

`scripts/seed.sh`는 v1의 비회원 요청 형식을 사용하므로 현재 v2 인증 API에서는 사용하지 않는다. JWT 로그인 흐름을 지원하도록 갱신하기 전까지 위의 `curl` 예시로 확인한다.

## DataGrip 연결

| 항목 | 값 |
| --- | --- |
| Host | `localhost` |
| Port | `.env`의 `DB_PORT` |
| Database | `.env`의 `MYSQL_DATABASE` |
| User | `.env`의 `MYSQL_USER` |
| Password | `.env`의 `MYSQL_PASSWORD` |

JPA가 `members`, `boards`, `comments`, `refresh_tokens` 테이블과 FK를 생성·갱신한다. 운영에서는 `ddl-auto=update` 대신 마이그레이션 도구와 `validate`를 사용한다.

## 문서

- [문서 안내](docs/README.md)
- [아키텍처](docs/architecture.md)
- [도메인 모델](docs/domain-model.md)
- [API 명세](docs/api-spec.md)
- [로컬 실행 및 운영](docs/operations.md)
- [구현 계획](docs/plan.md)
