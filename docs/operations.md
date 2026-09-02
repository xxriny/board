# 로컬 실행 및 운영 설계

## 구성 파일

```text
.env.example
.gitignore
build.gradle
settings.gradle
docker-compose.yml
src/main/resources/application.properties
```

실제 `.env`는 Git에서 제외한다. `.env.example`에는 다음 키만 예시값과 함께 둔다.

```dotenv
MYSQL_DATABASE=board_db
MYSQL_USER=board_user
MYSQL_PASSWORD=board_password
MYSQL_ROOT_PASSWORD=root_password
DB_HOST=localhost
DB_PORT=3306
```

## MySQL

Docker Compose 서비스 이름은 `mysql`, 이미지는 `mysql:8.0`, 컨테이너 포트는 3306이며 호스트 3306에 바인딩한다. named volume `board_mysql_data`를 `/var/lib/mysql`에 마운트한다.

health check는 다음 명령을 사용한다.

```text
mysqladmin ping -h localhost -u root -p${MYSQL_ROOT_PASSWORD}
```

## Boot/JPA 프로퍼티

`application.properties`는 환경변수를 다음 기본값과 연결한다.

```properties
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${MYSQL_DATABASE:board_db}?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
spring.datasource.username=${MYSQL_USER:board_user}
spring.datasource.password=${MYSQL_PASSWORD:board_password}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

DataSource, EntityManagerFactory, TransactionManager는 Spring Boot 자동 설정이 구성한다.

## 빌드와 실행

```bash
./gradlew clean test bootJar
set -a
source .env
set +a
java -jar build/libs/board.jar
```

개발 중에는 다음 명령으로 바로 실행할 수 있다.

```bash
./gradlew bootRun
```

Swagger UI는 `http://localhost:8080/swagger-ui/index.html`, OpenAPI JSON은 `http://localhost:8080/v3/api-docs`에서 확인한다.

## DataGrip

| 항목 | 값 |
| --- | --- |
| Host | `localhost` |
| Port | `.env`의 `DB_PORT` (기본 `3306`) |
| Database | `board_db` |
| User | `.env`의 `MYSQL_USER` |
| Password | `.env`의 `MYSQL_PASSWORD` |

MySQL 컨테이너 기동 후 Test Connection을 실행하고 `boards`, `comments` 테이블과 `comments.board_id` FK를 확인한다. DataGrip 다이어그램은 실제 DB 스키마를 기준으로 별도 관리하며 `docs/board.erd`를 DataGrip 내부 파일로 사용하지 않는다.

## 실행 순서

1. `.env.example`을 `.env`로 복사하고 로컬 비밀번호를 설정한다.
2. `docker compose up -d mysql`을 실행한다.
3. `docker compose ps`에서 MySQL이 healthy인지 확인한다.
4. `./gradlew clean test bootJar`를 실행한다.
5. `java -jar build/libs/board.jar` 또는 `./gradlew bootRun`으로 실행한다.
6. Swagger UI와 `/v3/api-docs`를 확인한다.
7. DataGrip에서 DB 연결과 FK를 확인한다.
