package com.xxrin.board.repository;

import com.xxrin.board.domain.Board;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Spring Data JPA 기반 게시글 Repository다. */
public interface BoardRepository extends JpaRepository<Board, Long> {

    @Modifying(flushAutomatically = true)
    @Query(value = "update boards set comment_count = comment_count + 1 where id = :boardId", nativeQuery = true)
    int incrementCommentCount(@Param("boardId") Long boardId);

    @Modifying(flushAutomatically = true)
    @Query(value = "update boards set comment_count = greatest(comment_count - 1, 0) where id = :boardId", nativeQuery = true)
    int decrementCommentCount(@Param("boardId") Long boardId);
}
