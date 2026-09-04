package com.xxrin.board.dto.response;

import com.xxrin.board.domain.Comment;
import java.time.LocalDateTime;

/** 댓글 조회 및 변경 결과 응답이다. */
public record CommentResponse(
        Long id,
        String content,
        String writer,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getAuthor().getNickname(),
                comment.getCreatedAt(),
                comment.getUpdatedAt());
    }
}
