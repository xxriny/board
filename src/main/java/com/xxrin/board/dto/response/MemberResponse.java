package com.xxrin.board.dto.response;

import com.xxrin.board.domain.Member;
import com.xxrin.board.domain.Role;
import java.time.LocalDateTime;

/** 외부에 공개 가능한 회원 정보다. */
public record MemberResponse(
        Long id,
        String email,
        String nickname,
        String phone,
        Role role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getEmail(),
                member.getNickname(),
                member.getPhone(),
                member.getRole(),
                member.getCreatedAt(),
                member.getUpdatedAt());
    }
}
