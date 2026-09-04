package com.xxrin.board.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.xxrin.board.domain.Member;
import com.xxrin.board.dto.request.LoginRequest;
import com.xxrin.board.exception.BusinessException;
import com.xxrin.board.exception.ErrorCode;
import com.xxrin.board.repository.MemberRepository;
import com.xxrin.board.repository.RefreshTokenRepository;
import com.xxrin.board.security.TokenService;
import com.xxrin.board.service.AuthService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

class LoginTest {

    @Test
    void returnsAccessTokenForValidCredentials() {
        MemberRepository members = mock(MemberRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        TokenService tokenService = mock(TokenService.class);
        Member member = Member.create(
                "user@example.com",
                "encoded-password",
                "사용자",
                "01012345678");
        ReflectionTestUtils.setField(member, "id", 1L);
        when(members.findByEmail("user@example.com")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("password123!", "encoded-password")).thenReturn(true);
        when(tokenService.createAccessToken(member)).thenReturn("access-token");

        when(tokenService.createRefreshToken()).thenReturn("refresh-token");
        when(tokenService.hashRefreshToken("refresh-token")).thenReturn("refresh-hash");

        var response = new AuthService(
                members,
                mock(RefreshTokenRepository.class),
                passwordEncoder,
                tokenService)
                .login(new LoginRequest(" USER@example.com ", "password123!"));

        assertThat(response.access().accessToken()).isEqualTo("access-token");
        assertThat(response.access().tokenType()).isEqualTo("Bearer");
        assertThat(response.access().expiresIn()).isEqualTo(900);
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
    }

    @Test
    void hidesWhetherEmailOrPasswordWasWrong() {
        MemberRepository members = mock(MemberRepository.class);

        assertThatThrownBy(() -> new AuthService(
                members,
                mock(RefreshTokenRepository.class),
                mock(PasswordEncoder.class),
                mock(TokenService.class))
                .login(new LoginRequest("missing@example.com", "password123!")))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.INVALID_CREDENTIALS));
    }
}
