package com.xxrin.board.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 게시글의 상태와 댓글 연관관계를 관리하는 JPA 엔티티다. */
@Entity
@Table(name = "boards")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Board extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    /*
     * Legacy: 비회원 비밀번호 방식 비교용
     *
     * @Column(nullable = false, length = 100)
     * private String writer;
     *
     * @Column(name = "password_hash", nullable = false, length = 60)
     * private String passwordHash;
     */

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member author;

    @Column(name = "view_count", nullable = false)
    private int viewCount;

//    @Formula("(select count(c.id) from comments c where c.board_id = id)")
//    private long commentCount;

    @Column(name = "comment_count", nullable = false, columnDefinition = "int default 0")
    private int commentCount;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC, id ASC")
    private final List<Comment> comments = new ArrayList<>();

    @Builder
    private Board(String title, String content, Member author) {
        this.title = title;
        this.content = content;
        this.author = author;
    }

    /** 수정 가능한 제목과 본문만 변경한다. */
    public void update(String title, String content) {
        this.title = title;
        this.content = content;
        touch();
    }

    /** 상세 조회 시 조회수를 한 건 증가시킨다. */
    public void increaseViewCount() {
        viewCount++;
    }

    public boolean isOwnedBy(Long memberId) {
        return author.getId().equals(memberId);
    }

    /** 댓글을 게시글에 연결하고 양방향 연관관계를 동기화한다. */
    public void addComment(Comment comment) {
        if (!comments.contains(comment)) {
            comments.add(comment);
        }
        comment.assignBoard(this);
    }

    /** 댓글 연결을 제거하고 양방향 연관관계를 동기화한다. */
    public void removeComment(Comment comment) {
        if (comments.remove(comment)) {
            comment.detachBoard(this);
        }
    }

    /** 외부 호출자가 컬렉션 구조를 직접 변경하지 못하도록 읽기 전용 뷰를 반환한다. */
    public List<Comment> getComments() {
        return Collections.unmodifiableList(comments);
    }
}
