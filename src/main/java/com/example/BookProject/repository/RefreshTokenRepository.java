package com.example.BookProject.repository;

import com.example.BookProject.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // 토큰 값으로 조회
    Optional<RefreshToken> findByTokenValue(String tokenValue);

    // 사용자 이메일로 조회 (기존 토큰 확인)
    Optional<RefreshToken> findByUserEmail(String userEmail);

    // 사용자 이메일로 모든 토큰 삭제 (로그아웃 시)
    void deleteByUserEmail(String userEmail);

    // 만료된 토큰 삭제 (배치 작업용)
    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}
