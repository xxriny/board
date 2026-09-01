package com.xxrin.board.repository;

import com.xxrin.board.domain.Comment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

/** EntityManager를 직접 사용하는 댓글 영속성 컴포넌트다. */
@Repository
public class CommentRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public Comment save(Comment comment) {
        entityManager.persist(comment);
        return comment;
    }
}
