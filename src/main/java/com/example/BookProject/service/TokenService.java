package com.example.BookProject.service;

import com.example.BookProject.domain.RefreshToken;
import com.example.BookProject.jwt.JwtTokenProvider;
import com.example.BookProject.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Refresh Token 관리 서비스
 * - DB 기반 토큰 저장/조회/삭제
 * - 로그아웃 시 토큰 무효화
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Refresh Token 생성 및 저장
     */
    public String createAndSaveRefreshToken(String userEmail) {
        // 기존 토큰이 있으면 삭제
        refreshTokenRepository.findByUserEmail(userEmail)
                .ifPresent(refreshTokenRepository::delete);

        // 새로운 Refresh Token 생성
        String tokenValue = jwtTokenProvider.createRefreshToken(userEmail);
        LocalDateTime expiresAt = jwtTokenProvider.getRefreshTokenExpiryDate();

        RefreshToken refreshToken = RefreshToken.builder()
                .tokenValue(tokenValue)
                .userEmail(userEmail)
                .expiresAt(expiresAt)
                .build();

        refreshTokenRepository.save(refreshToken);
        log.info("Refresh Token created for user: {}", userEmail);

        return tokenValue;
    }

    /**
     * Refresh Token 검증 및 조회
     */
    @Transactional(readOnly = true)
    public Optional<RefreshToken> validateRefreshToken(String tokenValue) {
        // JWT 형식 검증
        if (!jwtTokenProvider.validateToken(tokenValue)) {
            return Optional.empty();
        }

        // DB에서 토큰 조회
        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByTokenValue(tokenValue);

        if (tokenOpt.isEmpty()) {
            log.warn("Refresh Token not found in database");
            return Optional.empty();
        }

        RefreshToken token = tokenOpt.get();

        // 만료 검증
        if (token.isExpired()) {
            log.warn("Refresh Token expired for user: {}", token.getUserEmail());
            refreshTokenRepository.delete(token);
            return Optional.empty();
        }

        return Optional.of(token);
    }

    /**
     * 사용자의 모든 Refresh Token 삭제 (로그아웃)
     */
    public void deleteRefreshTokenByUserEmail(String userEmail) {
        refreshTokenRepository.deleteByUserEmail(userEmail);
        log.info("Refresh Token deleted for user: {}", userEmail);
    }

    /**
     * 만료된 토큰 정리 (스케줄러에서 호출)
     */
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        log.info("Expired refresh tokens cleaned up");
    }
}
