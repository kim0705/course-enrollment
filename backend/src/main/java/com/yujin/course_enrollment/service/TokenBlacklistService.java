package com.yujin.course_enrollment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;

/**
 * AccessToken 블랙리스트 서비스
 * 로그아웃한 accessToken을 Redis에 저장해 잔여 TTL 동안 재사용 차단
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final StringRedisTemplate redisTemplate;

    private static final String PREFIX = "BL:";

    /**
     * AccessToken을 블랙리스트에 추가
     * @param token 무효화할 accessToken
     * @param expiration 토큰 만료 시각
     */
    public void add(String token, Date expiration) {
        long remaining = expiration.getTime() - System.currentTimeMillis();

        if (remaining > 0) {
            redisTemplate.opsForValue().set(PREFIX + token, "1", Duration.ofMillis(remaining));
            log.debug("[TokenBlacklistService] AccessToken 블랙리스트 추가 - 잔여 TTL: {}ms", remaining);
        }
    }

    /**
     * AccessToken 블랙리스트 여부 확인
     * @param token 확인할 accessToken
     * @return 블랙리스트에 등록된 경우 true
     */
    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(PREFIX + token));
    }
}
