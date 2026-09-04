package com.xxrin.board.controller;

import com.xxrin.board.dto.request.LoginRequest;
import com.xxrin.board.dto.request.SignupRequest;
import com.xxrin.board.dto.response.AccessTokenResponse;
import com.xxrin.board.dto.response.ApiResponse;
import com.xxrin.board.dto.response.MemberResponse;
import com.xxrin.board.service.AuthService;
import com.xxrin.board.service.AuthTokens;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** 회원가입과 로그인 요청을 처리한다. */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "회원가입과 토큰 인증 API")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Value("${auth.cookie-secure:false}")
    private boolean cookieSecure;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MemberResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ApiResponse.success(
                authService.signup(request),
                "회원가입되었습니다.");
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AccessTokenResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthTokens tokens = authService.login(request);
        return withRefreshCookie(tokens, "로그인되었습니다.");
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AccessTokenResponse>> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshToken) {
        return withRefreshCookie(
                authService.refresh(refreshToken),
                "Access Token이 재발급되었습니다.");
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken) {
        authService.logout(refreshToken);
        return clearedCookie("로그아웃되었습니다.");
    }

    @PostMapping("/logout-all")
    public ResponseEntity<ApiResponse<Void>> logoutAll(@AuthenticationPrincipal Jwt jwt) {
        authService.logoutAll(Long.valueOf(jwt.getSubject()));
        return clearedCookie("모든 기기에서 로그아웃되었습니다.");
    }

    private ResponseEntity<ApiResponse<AccessTokenResponse>> withRefreshCookie(
            AuthTokens tokens,
            String message) {
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        refreshCookie(tokens.refreshToken()).build().toString())
                .body(ApiResponse.success(tokens.access(), message));
    }

    private ResponseEntity<ApiResponse<Void>> clearedCookie(String message) {
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        refreshCookie("").maxAge(Duration.ZERO).build().toString())
                .body(ApiResponse.success(null, message));
    }

    private ResponseCookie.ResponseCookieBuilder refreshCookie(String token) {
        return ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/api/auth")
                .maxAge(Duration.ofDays(14));
    }
}
