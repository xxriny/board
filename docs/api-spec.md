# REST API 명세

## 공통 형식

Content-Type은 `application/json`이다.

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

### BoardUpdateRequest

- `title`: `@NotBlank`, `@Size(max = 200)`
- `content`: `@Size(max = 10000)`, null/빈 문자열 허용

작성자는 수정하지 않는다.

### CommentCreateRequest

- `content`: `@NotBlank`, `@Size(max = 1000)`
- `writer`: `@NotBlank`, `@Size(max = 100)`

### CommentUpdateRequest

- `content`: `@NotBlank`, `@Size(max = 1000)`

댓글 작성자와 소속 게시글은 수정하지 않는다.

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

### 게시글 상세

조회 성공 시 `viewCount`를 1 증가시킨 값을 반환한다. 댓글은 `createdAt ASC`, 동률이면 `id ASC` 순으로 포함한다.

### 댓글 삭제

Repository는 `boardId`와 `commentId`를 동시에 조건으로 사용한다. 댓글이 없거나 해당 게시글 소속이 아니면 동일하게 404를 반환한다.

### 댓글 수정

Repository는 `boardId`와 `commentId`를 동시에 조건으로 조회한다. 댓글 내용만 수정하고 작성자, 소속 게시글, 생성 시각은 유지하며 `updatedAt`을 갱신한다. 댓글이 없거나 해당 게시글 소속이 아니면 404를 반환한다.

## 오류 매핑

| 조건 | 상태 | message |
| --- | --- | --- |
| Board 없음 | 404 | `게시글을 찾을 수 없습니다.` |
| Comment 없음/소속 불일치 | 404 | `댓글을 찾을 수 없습니다.` |
| Bean Validation 실패 | 400 | `입력값이 올바르지 않습니다.` |
| 읽을 수 없는 JSON/타입 불일치 | 400 | `요청 본문을 확인해 주세요.` |
| 처리되지 않은 예외 | 500 | `서버 내부 오류가 발생했습니다.` |

`GlobalExceptionHandler`는 내부 예외 메시지와 스택 트레이스를 응답에 노출하지 않는다.

## OpenAPI 태그

- `Board`: 게시글 생성, 목록, 상세, 수정, 삭제
- `Comment`: 댓글 생성, 목록, 삭제

각 Controller 메서드에 `@Operation`과 성공, 400, 404 응답의 `@ApiResponse`를 선언한다.
