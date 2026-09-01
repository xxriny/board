package com.xxrin.board.dto.response;

import com.xxrin.board.domain.Board;
import java.time.LocalDateTime;

/** 게시글 목록 및 생성·수정 결과에 사용하는 응답이다. */
public record BoardResponse(
        Long id,
        String title,
        String content,
        String writer,
        int viewCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static BoardResponse from(Board board) {
        return new BoardResponse(
                board.getId(),
                board.getTitle(),
                board.getContent(),
                board.getWriter(),
                board.getViewCount(),
                board.getCreatedAt(),
                board.getUpdatedAt());
    }
}
