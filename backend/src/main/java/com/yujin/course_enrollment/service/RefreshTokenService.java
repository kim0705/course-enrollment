package com.yujin.course_enrollment.service;

import com.yujin.course_enrollment.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

/**
 * RefreshToken 서비스
 * Redis에 RefreshToken을 저장/조회/삭제
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final StringRedisTemplate redisTemplate;

    private static final Duration TTL = Duration.ofDays(7);
    private static final String PREFIX = "RT:";

    /**
     * RefreshToken 생성 후 Redis에 저장
     * @param userId 사용자 ID
     * @return 생성된 refreshToken (UUID)
     */
    public String save(Long userId) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(PREFIX + token, String.valueOf(userId), TTL);

        log.debug("[RefreshTokenService] RefreshToken 저장 - userId: {}", userId);
        return token;
    }

    /**
     * RefreshToken 검증 후 userId 반환
     * @param token refreshToken
     * @return 토큰에 매핑된 사용자 ID
     * @throws BusinessException 유효하지 않은 토큰 (401)
     */
    public Long getUserId(String token) {
        String value = redisTemplate.opsForValue().get(PREFIX + token);

        if (value == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "유효하지 않은 refreshToken입니다.");
        }

        return Long.parseLong(value);
    }

    /**
     * RefreshToken 삭제
     * @param token 삭제할 refreshToken
     */
    public void delete(String token) {
        redisTemplate.delete(PREFIX + token);

        log.debug("[RefreshTokenService] RefreshToken 삭제 완료");
    }
}
