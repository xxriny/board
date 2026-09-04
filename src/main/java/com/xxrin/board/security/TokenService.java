package com.xxrin.board.security;

import com.xxrin.board.domain.Member;
import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

/** 짧게 사용하는 Access JWT를 발급한다. */
@Service
@RequiredArgsConstructor
public class TokenService {

    public static final long ACCESS_TOKEN_SECONDS = 900;

    private final JwtEncoder jwtEncoder;

    private final SecureRandom secureRandom = new SecureRandom();

    public String createAccessToken(Member member) {
        Instant issuedAt = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(member.getId().toString())
                .claim("role", member.getRole().name())
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plusSeconds(ACCESS_TOKEN_SECONDS))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims))
                .getTokenValue();
    }

    public String createRefreshToken() {
        byte[] token = new byte[32];
        secureRandom.nextBytes(token);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(token);
    }

    public String hashRefreshToken(String refreshToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(
                    digest.digest(refreshToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256을 사용할 수 없습니다.", exception);
        }
    }
}
