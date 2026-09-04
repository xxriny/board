# REST API 명세

## 공통 형식

Content-Type은 `application/json`이며 외부 환경에서는 HTTPS를 사용한다. 성공과 오류는 모두 `ApiResponse<T>`로 감싼다.

성공 응답:

```json
{
  "success": true,
  "data": {},
  "message": "요청이 성공했습니다."
}
```

오류 응답:

```json
{
  "success": false,
  "data": null,
  "code": "BOARD_NOT_FOUND",
  "message": "게시글을 찾을 수 없습니다."
}
```

## 인증

- Access Token: 로그인·재발급 응답 JSON, 15분
- Refresh Token: `refreshToken` HttpOnly 쿠키, 14일
- 인증 헤더: `Authorization: Bearer <access-token>`
- 게시글·댓글 조회와 회원가입·로그인·재발급·현재 기기 로그아웃은 공개한다.
- 게시글·댓글 생성과 내 정보 API는 로그인이 필요하다.
- 게시글·댓글 수정·삭제는 작성자만 가능하다.

## 인증 및 회원 API

| Method | URL | 인증 | 성공 상태 | 응답 data |
|---|---|---|---:|---|
| POST | `/api/auth/signup` | 공개 | 201 | `MemberResponse` |
| POST | `/api/auth/login` | 공개 | 200 | `AccessTokenResponse` |
| POST | `/api/auth/refresh` | Refresh 쿠키 | 200 | `AccessTokenResponse` |
| POST | `/api/auth/logout` | Refresh 쿠키 | 200 | null |
| POST | `/api/auth/logout-all` | Access Token | 200 | null |
| GET | `/api/members/me` | Access Token | 200 | `MemberResponse` |
| PUT | `/api/members/me` | Access Token | 200 | `MemberResponse` |

회원가입 요청은 `email`, `password`, `nickname`, `phone`을 받는다. 비밀번호는 8~72자이며 영문, 숫자, 공백이 아닌 특수문자를 각각 포함해야 한다. 이메일은 소문자, 전화번호는 하이픈 없는 숫자로 정규화한다.

로그인은 이메일이나 비밀번호 중 무엇이 틀렸는지 구분하지 않고 `INVALID_CREDENTIALS`를 반환한다. 재발급은 Refresh Token을 회전하므로 사용한 토큰은 다시 사용할 수 없다.

## 게시글과 댓글 API

| Method | URL | 인증 | 성공 상태 | 응답 data |
|---|---|---|---:|---|
| POST | `/api/boards` | Access Token | 201 | `BoardResponse` |
| GET | `/api/boards?page=0&size=10` | 공개 | 200 | `PageResponse<BoardResponse>` |
| GET | `/api/boards/{id}` | 공개 | 200 | `BoardDetailResponse` |
| PUT | `/api/boards/{id}` | 작성자 | 200 | `BoardResponse` |
| DELETE | `/api/boards/{id}` | 작성자 | 200 | null |
| POST | `/api/boards/{boardId}/comments` | Access Token | 201 | `CommentResponse` |
| GET | `/api/boards/{boardId}/comments` | 공개 | 200 | `List<CommentResponse>` |
| PUT | `/api/boards/{boardId}/comments/{commentId}` | 작성자 | 200 | `CommentResponse` |
| DELETE | `/api/boards/{boardId}/comments/{commentId}` | 작성자 | 200 | null |

`BoardCreateRequest`는 `title`, `content`만 받고 `BoardUpdateRequest`도 같은 필드를 받는다. `CommentCreateRequest`와 `CommentUpdateRequest`는 `content`만 받는다. 작성자 닉네임은 인증 회원에서 가져오며 요청으로 받지 않는다.

게시글 목록은 `createdAt DESC, id DESC`로 정렬한다. 상세 조회는 조회수를 1 증가시키고 댓글을 `createdAt ASC, id ASC`로 반환한다. 댓글 생성·삭제는 같은 트랜잭션에서 게시글의 `comment_count`를 원자적으로 증감한다.

## 오류 코드

| code | 상태 |
|---|---:|
| `VALIDATION_FAILED`, `INVALID_REQUEST` | 400 |
| `INVALID_CREDENTIALS`, `INVALID_ACCESS_TOKEN`, `INVALID_REFRESH_TOKEN` | 401 |
| `FORBIDDEN_RESOURCE` | 403 |
| `MEMBER_NOT_FOUND`, `BOARD_NOT_FOUND`, `COMMENT_NOT_FOUND`, `API_NOT_FOUND` | 404 |
| `METHOD_NOT_ALLOWED` | 405 |
| `DUPLICATE_EMAIL`, `DUPLICATE_NICKNAME`, `DUPLICATE_PHONE` | 409 |
| `UNSUPPORTED_MEDIA_TYPE` | 415 |
| `INTERNAL_SERVER_ERROR` | 500 |

`GlobalExceptionHandler`는 BusinessException과 MVC 예외를 변환한다. Security 필터에서 발생한 401/403은 `AuthenticationEntryPoint`와 `AccessDeniedHandler`가 같은 JSON 형식으로 변환한다.

## OpenAPI

springdoc이 `/v3/api-docs`와 `/swagger-ui/index.html`을 제공한다. Controller에는 Board, Comment, Auth, Member 태그와 주요 상태를 선언한다.
