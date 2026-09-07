# Board API 문서 안내

이 디렉터리는 Spring Boot 회원 기반 게시판 REST API의 설계, 운영 계약과 구현 기록을 역할별로 관리한다. 전체 실행 방법과 v1 대비 추가·변경·삭제 내역은 루트의 [`README.md`](../README.md)에서 확인한다.

| 문서 | 역할 |
| --- | --- |
| [`architecture.md`](architecture.md) | 런타임, 계층, Spring Boot 자동 설정 및 주요 기술 결정 |
| [`domain-model.md`](domain-model.md) | Member/Board/Comment/RefreshToken 모델, 관계, 소유권과 영속성 규칙 |
| [`api-spec.md`](api-spec.md) | 인증·회원·게시글·댓글 엔드포인트, `ApiResult` 응답과 오류 계약 |
| [`operations.md`](operations.md) | MySQL, 환경변수, JWT 비밀키, Boot 실행, Swagger, DataGrip 및 배포 보안 경계 |
| [`plan.md`](plan.md) | v2 마이그레이션 완료 상태와 후속 작업 |
| [`board.erd`](board.erd) | ERD Editor 3.0 형식의 논리·물리 ERD |

구현 시 루트의 [`AGENTS.md`](../AGENTS.md)를 먼저 적용하고, 문서 간 충돌이 있으면 API 계약, 도메인 규칙, 아키텍처, 운영 절차, 구현 계획 순으로 판단한다. v1의 비회원 비밀번호 방식과 WAR 운영 방법은 `v1.0.0` 태그에서 확인한다. 로컬 authentication 메모와 실제 `.env`는 보안상 Git에서 제외한다.
