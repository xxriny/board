package com.xxrin.board.dto.request;

import com.xxrin.board.domain.Board;
import com.xxrin.board.domain.Member;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 게시글 생성 요청이다. */
public record BoardCreateRequest(
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 200, message = "제목은 200자 이하여야 합니다.")
        String title,

        @Size(max = 10000, message = "내용은 10000자 이하여야 합니다.")
        String content) {

    /*
     * Legacy: 비회원 비밀번호 방식 비교용
     * String writer,
     * String password
     */

    public Board toEntity(Member author) {
        return Board.builder()
                .title(title)
                .content(content)
                .author(author)
                .build();
    }
}
