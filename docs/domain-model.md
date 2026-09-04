# 도메인 및 데이터 모델

## 관계

```text
members 1 ─── 0..N boards
members 1 ─── 0..N comments
members 1 ─── 0..N refresh_tokens
boards  1 ─── 0..N comments
```

게시글과 댓글은 반드시 한 회원이 작성한다. 댓글은 반드시 한 게시글에 속하며 부모 댓글 연관관계는 두지 않는다.

## Member

| Java 필드 | 물리명 | 제약 |
|---|---|---|
| `id` | `id` | PK, identity |
| `email` | `email` | not null, unique |
| `passwordHash` | `password_hash` | not null, BCrypt |
| `nickname` | `nickname` | not null, unique |
| `phone` | `phone` | not null, unique |
| `role` | `role` | not null, `USER` |
| `createdAt` | `created_at` | not null |
| `updatedAt` | `updated_at` | not null |

이메일은 소문자, 전화번호는 하이픈 없는 숫자로 저장한다. 이메일과 role은 수정하지 않고 `updateProfile`로 닉네임과 전화번호만 변경한다.

## RefreshToken

| Java 필드 | 물리명 | 제약 |
|---|---|---|
| `id` | `id` | PK, identity |
| `member` | `member_id` | FK, not null, lazy |
| `tokenHash` | `token_hash` | SHA-256, unique |
| `expiresAt` | `expires_at` | not null |
| `createdAt` | `created_at` | not null |
| `updatedAt` | `updated_at` | not null |

로그인 기기마다 행을 하나 생성한다. 원문은 저장하지 않으며 재발급 시 기존 행을 소비하고 새 행을 만든다.

## Board

| Java 필드 | 물리명 | 제약 |
|---|---|---|
| `id` | `id` | PK, identity |
| `title` | `title` | not null, 최대 200자 |
| `content` | `content` | TEXT, null 허용 |
| `author` | `member_id` | FK, not null, lazy |
| `viewCount` | `view_count` | not null, 기본값 0 |
| `commentCount` | `comment_count` | not null, 기본값 0 |
| `createdAt` | `created_at` | not null |
| `updatedAt` | `updated_at` | not null |

`writer`와 게시글 `password_hash`는 사용하지 않는다. API의 writer는 `author.nickname`에서 변환한다. 수정·삭제 전에 `isOwnedBy(memberId)`로 소유자를 확인한다.

## Comment

| Java 필드 | 물리명 | 제약 |
|---|---|---|
| `id` | `id` | PK, identity |
| `content` | `content` | not null, 최대 1000자 |
| `author` | `member_id` | FK, not null, lazy |
| `board` | `board_id` | FK, not null, lazy |
| `createdAt` | `created_at` | not null |
| `updatedAt` | `updated_at` | not null |

`writer`와 댓글 `password_hash`는 사용하지 않는다. API의 writer는 `author.nickname`에서 변환한다. 댓글은 `boardId`와 `commentId`를 함께 조회해 소속을 확인한다.

## 엔티티 구현 규칙

- 모든 시간 관리 엔티티는 `BaseTimeEntity`를 상속한다.
- public Setter를 두지 않고 도메인 메서드로 상태를 변경한다.
- 연관관계는 기본적으로 LAZY를 사용한다.
- Entity를 API 응답으로 직접 반환하지 않는다.
- 게시글 삭제 시 댓글은 cascade 삭제한다.
- 댓글 수는 댓글 생성·삭제 트랜잭션에서 원자적 UPDATE로 관리한다.
