package com.yujin.course_enrollment.service;

import com.yujin.course_enrollment.global.exception.BusinessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * RefreshToken 서비스 Redis 통합 테스트
 * 실제 Redis 연결로 저장/조회/삭제/TTL 동작 검증
 */
@SpringBootTest(properties = "jwt.secret=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
class RefreshTokenServiceIntegrationTest {

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @AfterEach
    void cleanup() {
        Set<String> keys = redisTemplate.keys("RT:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Test
    @DisplayName("RefreshToken 저장 - Redis에 정상 저장")
    void save_storesTokenInRedis() {
        // when
        String token = refreshTokenService.save(1L);

        // then
        String value = redisTemplate.opsForValue().get("RT:" + token);
        assertThat(value).isEqualTo("1");
    }

    @Test
    @DisplayName("RefreshToken 조회 - 저장된 토큰에서 userId 반환")
    void getUserId_returnsUserId() {
        // given
        String token = refreshTokenService.save(42L);

        // when
        Long userId = refreshTokenService.getUserId(token);

        // then
        assertThat(userId).isEqualTo(42L);
    }

    @Test
    @DisplayName("유효하지 않은 토큰 조회 - 401 예외")
    void getUserId_invalidToken_throws401() {
        assertThatThrownBy(() -> refreshTokenService.getUserId("nonexistent-token"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("유효하지 않은 refreshToken입니다.");
    }

    @Test
    @DisplayName("RefreshToken 삭제 - Redis에서 제거")
    void delete_removesTokenFromRedis() {
        // given
        String token = refreshTokenService.save(1L);

        // when
        refreshTokenService.delete(token);

        // then
        assertThat(redisTemplate.opsForValue().get("RT:" + token)).isNull();
    }

    @Test
    @DisplayName("RefreshToken TTL - 7일 이하로 설정")
    void save_setsTtlWithinSevenDays() {
        // when
        String token = refreshTokenService.save(1L);

        // then
        Long ttl = redisTemplate.getExpire("RT:" + token);
        assertThat(ttl)
                .isGreaterThan(0)
                .isLessThanOrEqualTo(Duration.ofDays(7).getSeconds());
    }
}
