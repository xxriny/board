package com.xxrin.board.service;

import com.xxrin.board.domain.Member;
import com.xxrin.board.domain.RefreshToken;
import com.xxrin.board.dto.request.LoginRequest;
import com.xxrin.board.dto.request.SignupRequest;
import com.xxrin.board.dto.response.AccessTokenResponse;
import com.xxrin.board.dto.response.MemberResponse;
import com.xxrin.board.exception.BusinessException;
import com.xxrin.board.exception.ErrorCode;
import com.xxrin.board.repository.MemberRepository;
import com.xxrin.board.repository.RefreshTokenRepository;
import com.xxrin.board.security.TokenService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 회원가입과 인증 유스케이스의 트랜잭션 경계를 관리한다. */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;

    private final RefreshTokenRepository refreshTokenRepository;

    private final PasswordEncoder passwordEncoder;

    private final TokenService tokenService;

    @Transactional
    public MemberResponse signup(SignupRequest request) {
        validateDuplicates(request);
        Member member = request.toEntity(passwordEncoder.encode(request.password()));
        return MemberResponse.from(memberRepository.saveAndFlush(member));
    }

    @Transactional
    public AuthTokens login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.normalizedEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));
        if (!passwordEncoder.matches(request.password(), member.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        return issueTokens(member);
    }
    /** 만료되거나 재사용된 Refresh Token도 삭제가 반영되도록 검증 예외는 롤백하지 않는다. */
    @Transactional(noRollbackFor = BusinessException.class)
    public AuthTokens refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        String tokenHash = tokenService.hashRefreshToken(refreshToken);
        RefreshToken stored = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));
        refreshTokenRepository.delete(stored);
        if (stored.isExpired(Instant.now())) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        return issueTokens(stored.getMember());
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenRepository.deleteByTokenHash(
                    tokenService.hashRefreshToken(refreshToken));
        }
    }

    @Transactional
    public void logoutAll(Long memberId) {
        refreshTokenRepository.deleteAllByMember_Id(memberId);
    }

    private AuthTokens issueTokens(Member member) {
        String refreshToken = tokenService.createRefreshToken();
        refreshTokenRepository.save(RefreshToken.issue(
                member,
                tokenService.hashRefreshToken(refreshToken),
                Instant.now().plusSeconds(14 * 24 * 60 * 60)));
        return new AuthTokens(
                AccessTokenResponse.bearer(tokenService.createAccessToken(member)),
                refreshToken);
    }

    private void validateDuplicates(SignupRequest request) {
        if (memberRepository.existsByEmail(request.normalizedEmail())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
        if (memberRepository.existsByNickname(request.normalizedNickname())) {
            throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
        }
        if (memberRepository.existsByPhone(request.normalizedPhone())) {
            throw new BusinessException(ErrorCode.DUPLICATE_PHONE);
        }
    }
}
