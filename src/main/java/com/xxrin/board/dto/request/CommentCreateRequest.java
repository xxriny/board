package com.xxrin.board.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 댓글 생성 요청이다. */
public record CommentCreateRequest(
        @NotBlank(message = "댓글 내용은 필수입니다.")
        @Size(max = 1000, message = "댓글 내용은 1000자 이하여야 합니다.")
        String content,
        @NotBlank(message = "작성자는 필수입니다.")
        @Size(max = 100, message = "작성자는 100자 이하여야 합니다.")
        String writer,
        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 4, max = 16, message = "비밀번호는 4자 이상 16자 이하여야 합니다.")
        String password) {
}
