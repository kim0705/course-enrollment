package com.yujin.course_enrollment.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Date;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * TokenBlacklist 서비스 Redis 통합 테스트
 * 실제 Redis 연결로 저장/확인/TTL 동작 검증
 */
@SpringBootTest(properties = "jwt.secret=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
class TokenBlacklistServiceIntegrationTest {

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @AfterEach
    void cleanup() {
        Set<String> keys = redisTemplate.keys("BL:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Test
    @DisplayName("블랙리스트 등록 - Redis에 정상 저장")
    void add_storesTokenInRedis() {
        // given
        String token = "test.access.token";
        Date expiration = new Date(System.currentTimeMillis() + 60_000);

        // when
        tokenBlacklistService.add(token, expiration);

        // then
        assertThat(redisTemplate.opsForValue().get("BL:" + token)).isEqualTo("1");
    }

    @Test
    @DisplayName("블랙리스트 확인 - 등록된 토큰 true 반환")
    void isBlacklisted_registeredToken_returnsTrue() {
        // given
        String token = "test.access.token";
        tokenBlacklistService.add(token, new Date(System.currentTimeMillis() + 60_000));

        // when & then
        assertThat(tokenBlacklistService.isBlacklisted(token)).isTrue();
    }

    @Test
    @DisplayName("블랙리스트 확인 - 미등록 토큰 false 반환")
    void isBlacklisted_unknownToken_returnsFalse() {
        assertThat(tokenBlacklistService.isBlacklisted("nonexistent.token")).isFalse();
    }

    @Test
    @DisplayName("블랙리스트 등록 - 만료된 토큰은 Redis 저장 생략")
    void add_expiredToken_skipsStorage() {
        // given
        String token = "expired.access.token";
        Date expiration = new Date(System.currentTimeMillis() - 1_000);

        // when
        tokenBlacklistService.add(token, expiration);

        // then
        assertThat(redisTemplate.opsForValue().get("BL:" + token)).isNull();
    }

    @Test
    @DisplayName("블랙리스트 등록 - TTL이 잔여 만료 시간 이하로 설정")
    void add_setsTtlBasedOnExpiration() {
        // given
        String token = "test.access.token";
        Date expiration = new Date(System.currentTimeMillis() + 60_000);

        // when
        tokenBlacklistService.add(token, expiration);

        // then
        Long ttl = redisTemplate.getExpire("BL:" + token);
        assertThat(ttl)
                .isGreaterThan(0)
                .isLessThanOrEqualTo(60);
    }
}
