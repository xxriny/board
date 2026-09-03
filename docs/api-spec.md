# REST API 명세

## 공통 형식

Content-Type은 `application/json`이다.

이 명세의 `http://` 예시는 로컬 개발용이다. 외부 환경에서는 반드시 HTTPS로 제공한다. 수정·삭제 비밀번호는 URL 쿼리 문자열이 아니라 JSON 본문으로만 전송한다.

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
  "message": "게시글을 찾을 수 없습니다."
}
```

검증 오류에서는 `data`가 필드명과 오류 메시지의 맵이다.

```json
{
  "success": false,
  "data": {
    "title": "제목은 필수입니다."
  },
  "message": "입력값이 올바르지 않습니다."
}
```

## 요청 DTO 검증

### BoardCreateRequest

- `title`: `@NotBlank`, `@Size(max = 200)`
- `content`: `@Size(max = 10000)`, null/빈 문자열 허용
- `writer`: `@NotBlank`, `@Size(max = 100)`
- `password`: `@NotBlank`, `@Size(min = 4, max = 16)`

### BoardUpdateRequest

- `title`: `@NotBlank`, `@Size(max = 200)`
- `content`: `@Size(max = 10000)`, null/빈 문자열 허용
- `password`: `@NotBlank`, `@Size(min = 4, max = 16)`

작성자는 수정하지 않는다. 비밀번호는 게시글의 BCrypt 해시와 일치해야 하며 응답에는 노출하지 않는다.

### CommentCreateRequest

- `content`: `@NotBlank`, `@Size(max = 1000)`
- `writer`: `@NotBlank`, `@Size(max = 100)`
- `password`: `@NotBlank`, `@Size(min = 4, max = 16)`

### CommentUpdateRequest

- `content`: `@NotBlank`, `@Size(max = 1000)`
- `password`: `@NotBlank`, `@Size(min = 4, max = 16)`

댓글 작성자와 소속 게시글은 수정하지 않는다. 비밀번호는 댓글의 BCrypt 해시와 일치해야 하며 응답에는 노출하지 않는다.

### PasswordRequest

- `password`: `@NotBlank`, `@Size(min = 4, max = 16)`

게시글과 댓글 삭제 요청의 JSON 본문에 사용한다.

리소스 비밀번호는 계정 인증이나 권한 관리 기능이 아니다. 현재 API에는 로그인, 사용자별 권한, 요청 횟수 제한이 없으므로 인터넷 공개 서비스에서는 별도의 인증·인가와 rate limit을 앞단 또는 애플리케이션에 추가해야 한다.

## 엔드포인트

| Method | URL | 성공 상태 | 응답 data |
| --- | --- | --- | --- |
| POST | `/api/boards` | 201 | `BoardResponse` |
| GET | `/api/boards?page=0&size=10` | 200 | `PageResponse<BoardResponse>` |
| GET | `/api/boards/{id}` | 200 | `BoardDetailResponse` |
| PUT | `/api/boards/{id}` | 200 | `BoardResponse` |
| DELETE | `/api/boards/{id}` | 200 | null |
| POST | `/api/boards/{boardId}/comments` | 201 | `CommentResponse` |
| GET | `/api/boards/{boardId}/comments` | 200 | `List<CommentResponse>` |
| PUT | `/api/boards/{boardId}/comments/{commentId}` | 200 | `CommentResponse` |
| DELETE | `/api/boards/{boardId}/comments/{commentId}` | 200 | null |

### 게시글 목록

- `page` 기본값 0, 최소 0
- `size` 기본값 10, 최소 1, 최대 100
- 정렬: `createdAt DESC`, 동률이면 `id DESC`
- `PageResponse`: `content`, `page`, `size`, `totalElements`, `totalPages`
- `BoardResponse`의 `commentCount`에 `boards.comment_count`로 관리되는 각 게시글의 댓글 수를 포함한다. 값은 댓글 생성·삭제 트랜잭션에서 원자적 UPDATE로 증감한다.

### 게시글 상세

조회 성공 시 `viewCount`를 1 증가시킨 값을 반환한다. `commentCount`와 댓글 목록을 함께 제공하며 댓글은 `createdAt ASC`, 동률이면 `id ASC` 순으로 포함한다.

### 게시글 삭제

JSON 본문의 필수 `password`를 게시글의 BCrypt 해시와 비교한다. 일치할 때만 게시글과 연관 댓글을 cascade 삭제한다.

### 댓글 삭제

Repository는 `boardId`와 `commentId`를 동시에 조건으로 사용한다. JSON 본문의 필수 `password`를 댓글의 BCrypt 해시와 비교하고, 일치할 때만 삭제한다. 댓글이 없거나 해당 게시글 소속이 아니면 동일하게 404를 반환한다.

### 댓글 수정

Repository는 `boardId`와 `commentId`를 동시에 조건으로 조회한다. 비밀번호가 일치할 때만 댓글 내용을 수정하고 작성자, 소속 게시글, 생성 시각은 유지하며 `updatedAt`을 갱신한다. 댓글이 없거나 해당 게시글 소속이 아니면 404를 반환한다.

## 오류 매핑

| 조건 | 상태 | message |
| --- | --- | --- |
| Board 없음 | 404 | `게시글을 찾을 수 없습니다.` |
| 게시글 비밀번호 불일치 | 403 | `비밀번호가 일치하지 않습니다.` |
| 댓글 비밀번호 불일치 | 403 | `비밀번호가 일치하지 않습니다.` |
| Comment 없음/소속 불일치 | 404 | `댓글을 찾을 수 없습니다.` |
| Bean Validation 실패 | 400 | `입력값이 올바르지 않습니다.` |
| 읽을 수 없는 JSON/타입 불일치 | 400 | `요청 본문을 확인해 주세요.` |
| 존재하지 않는 URL | 404 | `요청한 경로를 찾을 수 없습니다.` |
| 처리되지 않은 예외 | 500 | `서버 내부 오류가 발생했습니다.` |

`GlobalExceptionHandler`는 내부 예외 메시지와 스택 트레이스를 응답에 노출하지 않는다.

## OpenAPI 태그

- `Board`: 게시글 생성, 목록, 상세, 수정, 삭제
- `Comment`: 댓글 생성, 목록, 수정, 삭제

Controller에는 `@Tag`, `@Operation`과 주요 오류 `@ApiResponse`를 선언한다. OpenAPI 문서는 springdoc이 `/v3/api-docs`에서 생성하고 Swagger UI는 `/swagger-ui/index.html`에서 제공한다.
