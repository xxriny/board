package com.xxrin.board.dto.response;

/** 로그인 또는 재발급으로 전달하는 Access Token 정보다. */
public record AccessTokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn) {

    public static AccessTokenResponse bearer(String accessToken) {
        return new AccessTokenResponse(accessToken, "Bearer", 900);
    }
}
