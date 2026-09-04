package com.xxrin.board.dto.request;

import com.xxrin.board.domain.Member;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Locale;

/** 이메일 회원가입 요청이다. */
public record SignupRequest(
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d\\s]).{8,72}$",
                message = "비밀번호는 8자 이상이며 영문, 숫자, 특수문자를 각각 포함해야 합니다.")
        String password,

        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(min = 2, max = 100, message = "닉네임은 2자 이상 100자 이하여야 합니다.")
        String nickname,

        @NotBlank(message = "전화번호는 필수입니다.")
        @Pattern(
                regexp = "^01[016789]-?\\d{3,4}-?\\d{4}$",
                message = "전화번호 형식이 올바르지 않습니다.")
        String phone) {

    public String normalizedEmail() {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    public String normalizedNickname() {
        return nickname.trim();
    }

    public String normalizedPhone() {
        return phone.replace("-", "");
    }

    public Member toEntity(String passwordHash) {
        return Member.create(
                normalizedEmail(),
                passwordHash,
                normalizedNickname(),
                normalizedPhone());
    }
}
