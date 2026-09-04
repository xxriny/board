package com.xxrin.board.repository;

import com.xxrin.board.domain.RefreshToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** 기기별 Refresh Token 해시를 관리한다. */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    void deleteByTokenHash(String tokenHash);

    void deleteAllByMember_Id(Long memberId);
}
