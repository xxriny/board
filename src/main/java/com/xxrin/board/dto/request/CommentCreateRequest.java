package com.xxrin.board.dto.request;

import com.xxrin.board.domain.Board;
import com.xxrin.board.domain.Comment;
import com.xxrin.board.domain.Member;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 댓글 생성 요청이다. */
public record CommentCreateRequest(
        @NotBlank(message = "댓글 내용은 필수입니다.")
        @Size(max = 1000, message = "댓글 내용은 1000자 이하여야 합니다.")
        String content) {

    /* Legacy: 비회원 비밀번호 방식 비교용 - String writer, String password */

    public Comment toEntity(Board board, Member author) {
        return Comment.builder()
                .content(content)
                .author(author)
                .board(board)
                .build();
    }
}
