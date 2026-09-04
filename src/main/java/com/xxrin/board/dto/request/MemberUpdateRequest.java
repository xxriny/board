package com.xxrin.board.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** 수정 가능한 회원 프로필 요청이다. */
public record MemberUpdateRequest(
        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(min = 2, max = 100, message = "닉네임은 2자 이상 100자 이하여야 합니다.")
        String nickname,

        @NotBlank(message = "전화번호는 필수입니다.")
        @Pattern(
                regexp = "^01[016789]-?\\d{3,4}-?\\d{4}$",
                message = "전화번호 형식이 올바르지 않습니다.")
        String phone) {

    public String normalizedNickname() {
        return nickname.trim();
    }

    public String normalizedPhone() {
        return phone.replace("-", "");
    }
}
