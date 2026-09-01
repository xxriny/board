package com.xxrin.board.repository;

import com.xxrin.board.domain.Board;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
}
