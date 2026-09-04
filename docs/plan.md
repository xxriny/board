# Board REST API v2 마이그레이션 계획 및 상태

## 현재 상태

v1 순수 Spring MVC/WAR 구현은 `v1.0.0` 태그에 보존한다. 현재 프로젝트 작업 대상은 Java 17, Spring Boot 3, Spring Data JPA, 내장 Tomcat과 실행형 `bootJar`를 사용하는 v2다.

v1의 최초 세부 작업 계획, WAR 빌드 명령과 수동 Spring 설정 기록은 현재 구현 기준이 아니다. 필요할 때는 `v1.0.0` 태그에서 확인한다.

## 완료된 범위

- [x] Spring Boot 애플리케이션 전환과 실행형 `board.jar` 구성
- [x] Spring Data JPA Repository 전환
- [x] Board와 1-depth Comment의 생성, 목록, 상세, 수정, 삭제 API
- [x] Spring Security 회원가입·로그인과 JWT 인증
- [x] BCrypt 회원 비밀번호 해시와 기기별 Refresh Token 회전
- [x] 회원 소유권 기반 게시글·댓글 생성·수정·삭제
- [x] `ApiResponse<T>`와 `ErrorCode` 기반 JSON 오류 처리
- [x] 게시글 상세 조회수 증가, 댓글 수 계산, 게시글 삭제 시 댓글 cascade 삭제
- [x] springdoc 기반 OpenAPI와 Swagger UI
- [x] MySQL Docker Compose, Testcontainers MySQL 통합 테스트
- [x] `./gradlew clean test bootJar`, `docker compose config` 검증

## 유지보수 원칙

- API 계약은 `api-spec.md`, 도메인 규칙은 `domain-model.md`, 실행·배포 규칙은 `operations.md`를 기준으로 한다.
- 기능 변경은 Repository, Service, Controller, DTO, 테스트를 한 세로 기능 단위로 함께 변경한다.
- 인증·소유권 검증, Refresh Token 회전, 댓글 소속 검증, 조회수 증가와 cascade 삭제처럼 영향이 큰 동작은 실패 테스트를 먼저 작성한다.
- Spring Boot, MySQL 이미지, springdoc을 포함한 의존성은 지원되는 보안 패치 버전으로 유지한다.

## 후속 작업

- [ ] 운영 프로필을 분리하고 `ddl-auto=validate`, 마이그레이션 도구, 운영 로그 정책을 적용한다.
- [ ] 공개 서비스가 필요하면 HTTPS, rate limit, Swagger/OpenAPI 접근 제어를 적용한다.
- [ ] MySQL Compose의 기본 비밀번호·외부 포트 바인딩을 로컬 개발 전용 설정으로 분리하고, 운영 이미지를 버전 또는 digest로 고정한다.
- [ ] Spring Boot 3.5 계열 지원 종료에 맞춰 지원되는 후속 버전 업그레이드 계획을 수립한다.
