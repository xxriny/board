package com.xxrin.board.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Locale;

/** 이메일과 비밀번호 로그인 요청이다. */
public record LoginRequest(
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,

        @NotBlank(message = "비밀번호는 필수입니다.")
        String password) {

    public String normalizedEmail() {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
