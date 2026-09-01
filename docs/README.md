# Board API 문서 안내

이 디렉터리는 Spring MVC 게시판 REST API의 설계와 구현 계획을 역할별로 관리한다.

| 문서 | 역할 |
| --- | --- |
| [`architecture.md`](architecture.md) | 런타임, 계층, Spring 컨텍스트 및 주요 기술 결정 |
| [`domain-model.md`](domain-model.md) | Board/Comment 모델, 관계, 영속성 규칙 |
| [`api-spec.md`](api-spec.md) | 엔드포인트, 요청 검증, 응답과 오류 계약 |
| [`operations.md`](operations.md) | MySQL, 환경변수, Tomcat, Swagger 및 DataGrip 실행 규칙 |
| [`plan.md`](plan.md) | TDD 기반 구현 순서와 완료 체크리스트 |
| [`board.erd`](board.erd) | ERD Editor 3.0 형식의 논리·물리 ERD |

구현 시 루트의 [`AGENTS.md`](../AGENTS.md)를 먼저 적용하고, 문서 간 충돌이 있으면 API 계약, 도메인 규칙, 아키텍처, 운영 절차, 구현 계획 순으로 판단한다.
