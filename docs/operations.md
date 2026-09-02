# 로컬 실행 및 운영 설계

## 구성 파일

```text
.env.example
.gitignore
build.gradle
settings.gradle
docker-compose.yml
src/main/resources/application.yml
```

실제 `.env`는 Git에서 제외한다. `.env.example`의 값은 **로컬 개발용 예시**이며 비밀값이 아니다. 복사한 뒤 각자 다른 비밀번호로 변경한다. 운영 비밀값은 `.env`나 추적 파일에 저장하지 않고 배포 환경의 secret 관리 수단으로 주입한다.

```dotenv
MYSQL_DATABASE=board_db
MYSQL_USER=board_user
MYSQL_PASSWORD=board_password
MYSQL_ROOT_PASSWORD=root_password
DB_HOST=localhost
DB_PORT=3306
```

## MySQL

Docker Compose 서비스 이름은 `mysql`, 이미지는 `mysql:8.0`, 컨테이너 포트는 3306이며 호스트의 `${DB_PORT:-3306}`에 바인딩한다. named volume `board_mysql_data`를 `/var/lib/mysql`에 마운트한다.

이 Compose 파일은 로컬 개발용이다. 환경변수가 없을 때 알려진 개발용 비밀번호와 모든 호스트 인터페이스 바인딩을 사용하므로 운영에 사용하지 않는다. 운영에서는 다음을 적용한다.

- MySQL 포트를 외부에 공개하지 않거나 `127.0.0.1` 또는 사설 네트워크에만 바인딩한다.
- `MYSQL_PASSWORD`, `MYSQL_ROOT_PASSWORD`를 충분히 강한 고유 값으로 제공하고 fallback 기본값에 의존하지 않는다.
- MySQL 이미지는 검증한 마이너 버전 또는 digest로 고정하고 정기적으로 갱신한다.

health check는 다음 명령을 사용한다.

```text
mysqladmin ping -h localhost -u root -p${MYSQL_ROOT_PASSWORD}
```

## Boot/JPA 프로퍼티

`application.yml`는 환경변수를 다음 설정과 연결한다. `MYSQL_PASSWORD`는 기본값이 없으므로 애플리케이션 실행 시 반드시 제공해야 한다.

```yaml
spring:
  config:
    import: optional:file:.env[.properties]
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${MYSQL_DATABASE:board_db}?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
    username: ${MYSQL_USER:board_user}
    password: ${MYSQL_PASSWORD}
  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true

logging:
  file:
    name: logs/board.log
  level:
    root: INFO
    com.xxrin.board: DEBUG
    org.hibernate.SQL: DEBUG
```

DataSource, EntityManagerFactory, TransactionManager는 Spring Boot 자동 설정이 구성한다.

위 설정은 현재 로컬 개발 기본값이다. 운영 프로필에서는 `spring.jpa.hibernate.ddl-auto=validate`와 마이그레이션 도구를 사용하고, `show-sql`, Hibernate SQL DEBUG, 애플리케이션 DEBUG 로그를 비활성화한다. 로그 저장 위치의 접근 권한과 보존 기간도 배포 환경에서 관리한다.

## 빌드와 실행

```bash
./gradlew clean test bootJar
java -jar build/libs/board.jar
```

`.env` 상대 경로를 읽을 수 있도록 프로젝트 루트에서 애플리케이션을 실행한다.

개발 중에는 다음 명령으로 바로 실행할 수 있다.

```bash
./gradlew bootRun
```

Swagger UI는 `http://localhost:8080/swagger-ui/index.html`, OpenAPI JSON은 `http://localhost:8080/v3/api-docs`에서 확인한다.

## 외부 배포 보안

현재 애플리케이션은 리소스 비밀번호 외에 계정 인증·인가와 요청 횟수 제한을 제공하지 않는다. 인터넷에 직접 공개하지 말고, 최소한 다음 경계를 구성한다.

1. TLS를 종료하는 리버스 프록시 뒤에 배치하고 HTTP를 HTTPS로 전환한다.
2. 애플리케이션 포트, MySQL 포트, Swagger UI와 `/v3/api-docs`의 접근 대상을 네트워크 정책으로 제한한다.
3. 수정·삭제 요청에는 rate limit 또는 별도의 인증·인가를 적용한다.
4. Spring Boot 및 직접 지정한 의존성을 지원되는 보안 패치 버전으로 갱신한다. Spring Boot 3.5 계열은 `3.5.16`이 마지막 OSS 릴리스이므로 후속 지원 계획을 수립한다.

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
