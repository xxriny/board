package com.xxrin.board.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 게시글 또는 댓글 삭제 시 사용하는 비밀번호 요청이다. */
public record PasswordRequest(
        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 4, max = 16, message = "비밀번호는 4자 이상 16자 이하여야 합니다.")
        String password) {
}
