package com.xxrin.board.controller;

import com.xxrin.board.dto.request.MemberUpdateRequest;
import com.xxrin.board.dto.response.ApiResult;
import com.xxrin.board.dto.response.MemberResponse;
import com.xxrin.board.service.MemberService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 로그인 회원의 프로필 API를 제공한다. */
@RestController
@RequestMapping("/api/members/me")
@Tag(name = "Member", description = "내 회원 정보 API")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping
    public ApiResult<MemberResponse> findMe(@AuthenticationPrincipal Jwt jwt) {
        return ApiResult.success(
                memberService.findMe(Long.valueOf(jwt.getSubject())),
                "회원 정보를 조회했습니다.");
    }

    @PutMapping
    public ApiResult<MemberResponse> updateMe(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody MemberUpdateRequest request) {
        return ApiResult.success(
                memberService.updateMe(Long.valueOf(jwt.getSubject()), request),
                "회원 정보가 수정되었습니다.");
    }
}
