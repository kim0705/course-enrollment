package com.yujin.course_enrollment.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT 유틸리티
 * AccessToken 생성, 파싱 및 만료 시각 조회를 담당
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * AccessToken 생성
     * @param userId 사용자 ID (subject)
     * @param role 사용자 역할 (claim)
     * @return 서명된 JWT 문자열
     */
    public String generateToken(Long userId, String role) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 토큰 파싱 및 서명 검증
     * @param token JWT 문자열
     * @return Claims (subject, role 등 payload)
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 토큰 만료 시각 반환
     * @param token JWT 문자열
     * @return 만료 시각
     */
    public Date getExpiration(String token) {
        return parseToken(token).getExpiration();
    }

    /* Base64 디코딩된 secret으로 HMAC-SHA 서명 키 생성 */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }
}
