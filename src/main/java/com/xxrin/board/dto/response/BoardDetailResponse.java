package com.xxrin.board.dto.response;

import com.xxrin.board.domain.Board;
import java.time.LocalDateTime;
import java.util.List;

/** 댓글을 포함한 게시글 상세 응답이다. */
public record BoardDetailResponse(
        Long id,
        String title,
        String content,
        String writer,
        int viewCount,
        long commentCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<CommentResponse> comments) {

    public static BoardDetailResponse from(Board board) {
        List<CommentResponse> comments = board.getComments().stream()
                .map(CommentResponse::from)
                .toList();
        return new BoardDetailResponse(
                board.getId(),
                board.getTitle(),
                board.getContent(),
                board.getWriter(),
                board.getViewCount(),
                board.getCommentCount(),
                board.getCreatedAt(),
                board.getUpdatedAt(),
                comments);
    }
}
