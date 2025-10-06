package com.example.BookProject.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.security.Key;
import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private final String secretKey = "aDRzNWY3ZzhqOWsxbDJtNG41cDZxOHM5dDB1MnYzNGI1Zjc4aDlpMGozaDRzNWY3ZzhasdasdasdasdfsdfsafdsfhsgdashdjaksfqOWs=";
    private final long tokenValidityInSeconds = 3600;

    @BeforeEach
    void setUp() {
        // Clock 주입 없이, 원래의 생성자를 그대로 사용합니다.
        jwtTokenProvider = new JwtTokenProvider(secretKey, tokenValidityInSeconds);
    }

    @DisplayName("만료된 JWT 토큰은 유효성 검증에 실패한다")
    @Test
    void validateToken_expired() {
        // Given
        Date expiredDate = new Date(new Date().getTime() - 10000); // ✅ 현재보다 10초 전 시간으로 만료일을 설정
        Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));

        String expiredToken = Jwts.builder()
                .setSubject("expired@example.com")
                .claim("auth", "ROLE_USER")
                .setIssuedAt(new Date())
                .setExpiration(expiredDate) // ✅ 의도적으로 과거의 만료 시간을 주입
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

        // When
        boolean isValid = jwtTokenProvider.validateToken(expiredToken);

        // Then
        // Thread.sleep() 없이 바로 검증 가능!
        assertFalse(isValid);
    }

    @DisplayName("유효한 JWT 토큰은 유효성 검증에 성공한다")
    @Test
    void validateToken_valid() {
        // Given
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                new User("test@example.com", "", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        String validToken = jwtTokenProvider.createToken(authentication);

        // When
        boolean isValid = jwtTokenProvider.validateToken(validToken);

        // Then
        assertTrue(isValid);
    }
}