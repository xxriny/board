package com.xxrin.board.repository;

import com.xxrin.board.domain.Board;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data JPA 기반 게시글 영속성 Repository다. */
public interface BoardRepository extends JpaRepository<Board, Long> {
}
