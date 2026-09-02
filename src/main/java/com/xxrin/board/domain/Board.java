package com.xxrin.board.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Formula;

/** 게시글의 상태와 댓글 연관관계를 관리하는 JPA 엔티티다. */
@Entity
@Table(name = "boards")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, length = 100)
    private String writer;

    @Column(name = "password_hash", nullable = false, length = 60)
    private String passwordHash;

    @Column(name = "view_count", nullable = false)
    private int viewCount;

    @Formula("(select count(c.id) from comments c where c.board_id = id)")
    private long commentCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC, id ASC")
    private final List<Comment> comments = new ArrayList<>();

    @Builder
    private Board(String title, String content, String writer, String passwordHash) {
        this.title = title;
        this.content = content;
        this.writer = writer;
        this.passwordHash = passwordHash;
    }

    /** 수정 가능한 제목과 본문만 변경한다. */
    public void update(String title, String content) {
        this.title = title;
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    /** 상세 조회 시 조회수를 한 건 증가시킨다. */
    public void increaseViewCount() {
        viewCount++;
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
    @PrePersist
    private void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

}
