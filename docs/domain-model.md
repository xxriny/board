# 도메인 및 데이터 모델

## ERD

편집 가능한 원본은 [`board.erd`](board.erd)이다. `name`에는 MySQL 물리명, `comment`에는 한글 논리명을 기록한다.

```text
boards 1 ───────── 0..N comments
  id PK                 id PK
                       board_id FK NOT NULL
```

게시글은 댓글이 없어도 존재할 수 있고, 댓글은 반드시 하나의 게시글에 속한다.

## Board

| 논리명 | Java 필드 | 물리명 | Java 타입 | 제약 |
| --- | --- | --- | --- | --- |
| 게시글 식별자 | `id` | `id` | `Long` | PK, identity |
| 게시글 제목 | `title` | `title` | `String` | not null, 최대 200자 |
| 게시글 내용 | `content` | `content` | `String` | TEXT, null 허용 |
| 게시글 작성자 | `writer` | `writer` | `String` | not null, 최대 100자 |
| 게시글 비밀번호 해시 | `passwordHash` | `password_hash` | `String` | not null, BCrypt 60자 |
| 조회수 | `viewCount` | `view_count` | `int` | not null, 기본값 0 |
| 댓글 수 | `commentCount` | 계산 필드 | `long` | `@Formula`, 읽기 전용 |
| 생성 시각 | `createdAt` | `created_at` | `LocalDateTime` | not null |
| 수정 시각 | `updatedAt` | `updated_at` | `LocalDateTime` | not null |

JPA 테이블명은 `boards`다. `comments`는 `@OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)`로 매핑하며 컬렉션은 빈 `ArrayList`로 초기화한다. `commentCount`는 별도 컬럼을 만들지 않고 Hibernate `@Formula`의 상관 서브쿼리로 조회한다.

게시글 비밀번호 원문은 저장하지 않는다. Service에서 BCrypt로 해시해 `password_hash`에 저장하고 수정·삭제 요청 시 입력값과 비교한다. 외부 응답 DTO에는 해시를 포함하지 않는다.

허용된 상태 변경 메서드:

- `update(String title, String content)`
- `increaseViewCount()`
- `addComment(Comment comment)`
- `removeComment(Comment comment)`

`@PrePersist`에서 생성/수정 시각과 조회수 기본값을 보장한다. `update(String title, String content)`는 수정 시각을 즉시 갱신해 API 응답과 저장값의 의미를 일치시키며, 조회수 증가는 수정 시각에 영향을 주지 않는다.

## Comment

| 논리명 | Java 필드 | 물리명 | Java 타입 | 제약 |
| --- | --- | --- | --- | --- |
| 댓글 식별자 | `id` | `id` | `Long` | PK, identity |
| 댓글 내용 | `content` | `content` | `String` | not null, 최대 1000자 |
| 댓글 작성자 | `writer` | `writer` | `String` | not null, 최대 100자 |
| 댓글 비밀번호 해시 | `passwordHash` | `password_hash` | `String` | not null, BCrypt 60자 |
| 게시글 식별자 | `board` | `board_id` | `Board` | FK, not null, lazy |
| 생성 시각 | `createdAt` | `created_at` | `LocalDateTime` | not null |
| 수정 시각 | `updatedAt` | `updated_at` | `LocalDateTime` | not null |

JPA 테이블명은 `comments`다. `board`는 `@ManyToOne(fetch = FetchType.LAZY, optional = false)`와 `@JoinColumn(name = "board_id", nullable = false)`로 매핑한다. 부모 댓글 필드는 만들지 않는다. 댓글 비밀번호 원문은 저장하지 않고 Service에서 BCrypt로 해시해 `password_hash`에 저장한다. 수정·삭제 시 입력 비밀번호와 비교하며 외부 응답에는 해시를 포함하지 않는다. 댓글 수정은 `update(String content)`로 내용과 `updatedAt`을 즉시 변경해 수정 응답에도 새 시각을 포함한다. 작성자와 소속 게시글은 유지한다.

## 엔티티 구현 규칙

- 두 엔티티 모두 Lombok `@Getter`, `@NoArgsConstructor(access = AccessLevel.PROTECTED)`, `@Builder`를 사용한다.
- public Setter를 만들지 않는다.
- 빌더로 외부에서 `id`, 시간 필드 및 조회수를 주입하지 못하게 생성 API를 제한한다.
- `toString`, `equals`, `hashCode`에 연관관계 필드를 포함하지 않는다.
- API 계층에 엔티티를 반환하지 않는다.
