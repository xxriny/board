package com.xxrin.board.service;

import com.xxrin.board.dto.response.AccessTokenResponse;

/** HTTP 응답과 쿠키로 나누어 전달할 인증 결과다. */
public record AuthTokens(
        AccessTokenResponse access,
        String refreshToken) {
}
