package com.xxrin.board.repository;

import com.xxrin.board.domain.Board;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/** EntityManager를 직접 사용하는 게시글 영속성 컴포넌트다. */
@Repository
public class BoardRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public Board save(Board board) {
        entityManager.persist(board);
        return board;
    }

    public List<Board> findPage(int page, int size) {
        return entityManager.createQuery(
                        "select b from Board b order by b.createdAt desc, b.id desc", Board.class)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    public long count() {
        return entityManager.createQuery("select count(b) from Board b", Long.class)
                .getSingleResult();
    }

    public Optional<Board> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Board.class, id));
    }

    public void delete(Board board) {
        entityManager.remove(board);
    }
}
