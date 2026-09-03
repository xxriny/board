# Spring Boot Board REST API

Java 17, Spring Boot 3, Spring Data JPA, Hibernate 6으로 구현한 게시판 REST API다. 게시글과 1-depth 댓글을 JSON으로 제공하며 내장 Tomcat으로 실행한다.

## 기술 구성

- Java 17, Gradle, Spring Boot 3
- Spring Web MVC, Spring Data JPA, Hibernate 6
- MySQL 8.0 Docker Compose
- Jackson, Jakarta Validation, Lombok
- springdoc OpenAPI 3 Swagger UI

v1 순수 Spring MVC/WAR 버전은 `v1.0.0` 태그에서 확인한다.

## 1. MySQL 실행

```bash
cp .env.example .env
docker compose up -d mysql
docker compose ps
```

`.env`의 비밀번호는 로컬 값으로 변경한다. `board-mysql` 상태가 `healthy`가 되면 애플리케이션에서 접속할 수 있다. Compose의 기본 비밀번호와 포트 공개 방식은 로컬 개발 전용이므로 운영 환경에서 사용하지 않는다.

## 2. 테스트 및 실행 파일 빌드

```bash
./gradlew clean test bootJar
```

테스트를 생략하고 실행 파일만 빠르게 다시 만들 때는 다음 명령을 사용한다.

```bash
./gradlew clean bootJar
```

결과물은 `build/libs/board.jar`이다.

## 3. 애플리케이션 실행

개발 중에는 Gradle로 바로 실행한다.

```bash
./gradlew bootRun
```

빌드된 JAR로 실행할 때는 다음 명령을 사용한다.

```bash
java -jar build/libs/board.jar
```

두 명령은 프로젝트 루트의 `.env`를 `application.yml`에서 불러오므로 프로젝트 루트에서 실행한다.

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- 게시글 API: `http://localhost:8080/api/boards`

## API 빠른 확인

```bash
curl -X POST http://localhost:8080/api/boards \
  -H 'Content-Type: application/json' \
  -d '{"title":"첫 글","content":"내용","writer":"작성자","password":"1234"}'

curl 'http://localhost:8080/api/boards?page=0&size=10'
curl http://localhost:8080/api/boards/1

curl -X PUT http://localhost:8080/api/boards/1 \
  -H 'Content-Type: application/json' \
  -d '{"title":"수정 제목","content":"수정 내용","password":"1234"}'

curl -X POST http://localhost:8080/api/boards/1/comments \
  -H 'Content-Type: application/json' \
  -d '{"content":"댓글","writer":"댓글 작성자","password":"1234"}'

curl http://localhost:8080/api/boards/1/comments

curl -X PUT http://localhost:8080/api/boards/1/comments/1 \
  -H 'Content-Type: application/json' \
  -d '{"content":"수정 댓글","password":"1234"}'

curl -X DELETE http://localhost:8080/api/boards/1/comments/1 \
  -H 'Content-Type: application/json' \
  -d '{"password":"1234"}'

curl -X DELETE http://localhost:8080/api/boards/1 \
  -H 'Content-Type: application/json' \
  -d '{"password":"1234"}'
```

게시글과 댓글 비밀번호는 BCrypt 해시로 저장하며 응답에 노출하지 않는다. 각 리소스의 수정·삭제 시 작성 당시 비밀번호가 필요하다. 이 방식은 계정 인증이나 요청 횟수 제한을 제공하지 않으므로, 예시는 로컬 개발용으로만 사용한다.
게시글 목록과 상세 응답에는 각 게시글의 `commentCount`가 포함된다. 이 값은 `boards.comment_count` 물리 컬럼에 저장하며 댓글 생성·삭제와 같은 트랜잭션에서 원자적 UPDATE로 증감한다.

## DataGrip 연결

| 항목 | 값 |
| --- | --- |
| Host | `localhost` |
| Port | `.env`의 `DB_PORT` (기본 `3306`) |
| Database | `.env`의 `MYSQL_DATABASE` (기본 `board_db`) |
| User | `.env`의 `MYSQL_USER` |
| Password | `.env`의 `MYSQL_PASSWORD` |

DataGrip의 MySQL 데이터 소스를 추가한 뒤 Test Connection을 실행한다. JPA가 애플리케이션 시작 시 `boards`, `comments` 테이블과 FK를 생성·갱신한다. DataGrip 다이어그램은 실제 DB 스키마를 기준으로 별도 관리하고, 저장소의 논리·물리 설계 원본은 `docs/board.erd`이다.

## 구성값

`src/main/resources/application.yml`은 프로젝트 루트의 Git 제외 파일 `.env`를 선택적으로 불러오고 `DB_HOST`, `DB_PORT`, `MYSQL_DATABASE`, `MYSQL_USER`, `MYSQL_PASSWORD` 값을 읽는다. Hibernate는 로컬 개발용으로 `ddl-auto=update`, OSIV 비활성화와 SQL 출력을 사용한다. 운영 환경에서는 파일 대신 배포 환경의 secret 관리 수단을 사용하고, 별도 프로필에서 스키마 검증·마이그레이션과 로그 수준을 설정한다. 외부 배포 보안 요구 사항은 [`docs/operations.md`](docs/operations.md)를 따른다.
