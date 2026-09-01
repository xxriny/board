package com.xxrin.board.repository;

import com.xxrin.board.domain.Comment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
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

    public List<Comment> findAllByBoardId(Long boardId) {
        return entityManager.createQuery("""
                        select c from Comment c where c.board.id = :boardId
                        order by c.createdAt asc, c.id asc
                        """, Comment.class)
                .setParameter("boardId", boardId)
                .getResultList();
    }

    public Optional<Comment> findByBoardIdAndId(Long boardId, Long commentId) {
        return entityManager.createQuery("""
                        select c from Comment c
                        where c.board.id = :boardId and c.id = :commentId
                        """, Comment.class)
                .setParameter("boardId", boardId)
                .setParameter("commentId", commentId)
                .getResultStream()
                .findFirst();
    }

    public void delete(Comment comment) {
        entityManager.remove(comment);
    }
}
