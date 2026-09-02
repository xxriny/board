# Spring MVC Board REST API

Spring Boot와 Spring Data JPA 없이 Java 17, Spring MVC 6, Hibernate 6으로 구현한 게시판 REST API다. 게시글과 1-depth 댓글을 JSON으로 제공하며 외부 Tomcat 10.1에 WAR로 배포한다.

## 기술 구성

- Java 17, Gradle WAR
- Spring MVC 6 Java Config (`web.xml` 없음)
- 순수 JPA `EntityManager`, Hibernate 6, HikariCP
- MySQL 8.0 Docker Compose
- Jackson, Jakarta Validation, Lombok
- Swagger Core OpenAPI 3, Swagger UI WebJar

## 1. MySQL 실행

```bash
cp .env.example .env
docker compose up -d mysql
docker compose ps
```

`.env`의 비밀번호는 로컬 값으로 변경한다. `board-mysql` 상태가 `healthy`가 되면 애플리케이션에서 접속할 수 있다.

## 2. 테스트 및 WAR 재빌드

```bash
./gradlew clean test war
```

테스트를 생략하고 WAR만 빠르게 다시 만들 때는 다음 명령을 사용한다.

```bash
./gradlew clean war
```

결과물은 `build/libs/board.war`이다. Spring Boot 내장 서버는 제공하지 않는다.

## 3. Tomcat 10.1 배포

JDK 17로 실행되는 Tomcat 10.1을 준비하고 다음과 같이 배포한다. 이미 배포된 WAR를 교체할 때는 Tomcat을 멈춘 뒤 기존 배포 산출물을 지우고 새 WAR를 복사한다.

```bash
set -a
source .env
set +a

"$CATALINA_HOME/bin/catalina.sh" stop
rm -rf "$CATALINA_HOME/webapps/board" "$CATALINA_HOME/webapps/board.war"
cp build/libs/board.war "$CATALINA_HOME/webapps/board.war"
```

Tomcat을 실행한다.

```bash
"$CATALINA_HOME/bin/catalina.sh" run
```

WAR 파일명에 따라 context path는 `/board`다.

- Swagger UI: `http://localhost:8080/board/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/board/v3/api-docs`
- 게시글 API: `http://localhost:8080/board/api/boards`

현재 OpenAPI server URL도 `/board`로 설정되어 있으므로 `board.war` 파일명을 유지한다. 다른 context path로 배포하려면 `src/main/resources/openapi.json`의 `servers` 값도 함께 변경한다.

## API 빠른 확인

```bash
curl -X POST http://localhost:8080/board/api/boards \
  -H 'Content-Type: application/json' \
  -d '{"title":"첫 글","content":"내용","writer":"작성자","password":"1234"}'

curl 'http://localhost:8080/board/api/boards?page=0&size=10'
curl http://localhost:8080/board/api/boards/1

curl -X PUT http://localhost:8080/board/api/boards/1 \
  -H 'Content-Type: application/json' \
  -d '{"title":"수정 제목","content":"수정 내용","password":"1234"}'

curl -X POST http://localhost:8080/board/api/boards/1/comments \
  -H 'Content-Type: application/json' \
  -d '{"content":"댓글","writer":"댓글 작성자","password":"1234"}'

curl http://localhost:8080/board/api/boards/1/comments

curl -X PUT http://localhost:8080/board/api/boards/1/comments/1 \
  -H 'Content-Type: application/json' \
  -d '{"content":"수정 댓글","password":"1234"}'

curl -X DELETE 'http://localhost:8080/board/api/boards/1/comments/1?password=1234'
curl -X DELETE 'http://localhost:8080/board/api/boards/1?password=1234'
```

게시글과 댓글 비밀번호는 BCrypt 해시로 저장하며 응답에 노출하지 않는다. 각 리소스의 수정·삭제 시 작성 당시 비밀번호가 필요하다.

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

`src/main/resources/db.properties`는 `DB_HOST`, `DB_PORT`, `MYSQL_DATABASE`, `MYSQL_USER`, `MYSQL_PASSWORD` 환경변수를 읽는다. Hibernate는 `hbm2ddl.auto=update`와 SQL 출력을 사용한다. 운영 환경에서는 저장소 기본 비밀번호를 사용하지 않는다.
