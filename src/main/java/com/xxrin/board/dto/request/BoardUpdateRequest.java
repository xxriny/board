package com.xxrin.board.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 게시글 수정 요청이다. */
public record BoardUpdateRequest(
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 200, message = "제목은 200자 이하여야 합니다.")
        String title,

        @Size(max = 10000, message = "내용은 10000자 이하여야 합니다.")
        String content,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 4, max = 16, message = "비밀번호는 4자 이상 16자 이하여야 합니다.")
        String password) {
}
