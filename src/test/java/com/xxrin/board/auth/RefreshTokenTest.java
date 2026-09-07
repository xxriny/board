package com.xxrin.board.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.xxrin.board.domain.Member;
import com.xxrin.board.domain.RefreshToken;
import com.xxrin.board.repository.MemberRepository;
import com.xxrin.board.repository.RefreshTokenRepository;
import com.xxrin.board.security.TokenService;
import com.xxrin.board.service.AuthService;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class RefreshTokenTest {

    @Test
    void consumesRefreshTokenAndIssuesAReplacement() {
        Member member = Member.create("user@example.com", "hash", "사용자", "01012345678");
        RefreshTokenRepository refreshTokens = mock(RefreshTokenRepository.class);
        TokenService tokenService = mock(TokenService.class);
        RefreshToken stored = RefreshToken.issue(
                member,
                "old-hash",
                Instant.now().plusSeconds(60));
        when(tokenService.hashRefreshToken("old-token")).thenReturn("old-hash");
        when(refreshTokens.findByTokenHash("old-hash")).thenReturn(Optional.of(stored));
        when(tokenService.createAccessToken(member)).thenReturn("new-access");
        when(tokenService.createRefreshToken()).thenReturn("new-refresh");
        when(tokenService.hashRefreshToken("new-refresh")).thenReturn("new-hash");

        var result = new AuthService(
                mock(MemberRepository.class),
                refreshTokens,
                mock(PasswordEncoder.class),
                tokenService)
                .refresh("old-token");

        assertThat(result.access().accessToken()).isEqualTo("new-access");
        assertThat(result.refreshToken()).isEqualTo("new-refresh");
        verify(refreshTokens).delete(stored);
        verify(refreshTokens).save(any(RefreshToken.class));
    }
}
