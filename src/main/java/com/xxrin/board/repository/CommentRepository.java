package com.xxrin.board.repository;

import com.xxrin.board.domain.Comment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data JPA 기반 댓글 Repository다. */
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByBoard_IdOrderByCreatedAtAscIdAsc(Long boardId);

    Optional<Comment> findByBoard_IdAndId(Long boardId, Long commentId);
}
