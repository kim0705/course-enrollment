package com.yujin.course_enrollment.security;

import com.yujin.course_enrollment.service.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.http.MediaType;

/**
 * /api/auth/refresh 엔드포인트 통합 테스트
 * 실제 Redis 연결로 token rotation 동작 검증
 */
@SpringBootTest(properties = "jwt.secret=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
@AutoConfigureMockMvc
class AuthRefreshIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

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
    @DisplayName("refreshToken 쿠키 없음 - 401")
    void noRefreshToken_returns401() throws Exception {
        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("유효하지 않은 refreshToken - 401")
    void invalidRefreshToken_returns401() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("refreshToken", "invalid-token")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("유효한 refreshToken - 200 + 새 accessToken/refreshToken 쿠키 발급")
    void validRefreshToken_returns200_withNewCookies() throws Exception {
        // given: userId=1 (creator_a, data.sql에 존재)
        String oldToken = refreshTokenService.save(1L);

        // when & then
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("refreshToken", oldToken)))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    List<String> cookies = result.getResponse().getHeaders("Set-Cookie");
                    assertThat(cookies)
                            .anyMatch(c -> c.startsWith("accessToken="))
                            .anyMatch(c -> c.startsWith("refreshToken="));
                });

        // then: 기존 refreshToken Redis에서 삭제 (rotation)
        assertThat(redisTemplate.opsForValue().get("RT:" + oldToken)).isNull();
    }

    @Test
    @DisplayName("로그인 성공 - 200 + accessToken/refreshToken 쿠키 발급")
    void login_success_returns200WithCookies() throws Exception {
        // given: creator_a / Test1234! (data.sql)
        String body = "{\"username\":\"creator_a\",\"password\":\"Test1234!\"}";

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    List<String> cookies = result.getResponse().getHeaders("Set-Cookie");
                    assertThat(cookies)
                            .anyMatch(c -> c.startsWith("accessToken=") && !c.contains("Max-Age=0"))
                            .anyMatch(c -> c.startsWith("refreshToken=") && !c.contains("Max-Age=0"));
                });
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 비밀번호 → 401")
    void login_wrongPassword_returns401() throws Exception {
        // given
        String body = "{\"username\":\"creator_a\",\"password\":\"wrongpassword\"}";

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("로그아웃 - 200 + 쿠키 만료 + Redis 삭제")
    void logout_expiresCookiesAndDeletesToken() throws Exception {
        // given
        String refreshToken = refreshTokenService.save(1L);

        // when & then
        mockMvc.perform(post("/api/auth/logout")
                        .cookie(new Cookie("refreshToken", refreshToken)))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    List<String> cookies = result.getResponse().getHeaders("Set-Cookie");
                    assertThat(cookies)
                            .anyMatch(c -> c.startsWith("accessToken=") && c.contains("Max-Age=0"))
                            .anyMatch(c -> c.startsWith("refreshToken=") && c.contains("Max-Age=0"));
                });

        // then: Redis에서 refreshToken 삭제 확인
        assertThat(redisTemplate.opsForValue().get("RT:" + refreshToken)).isNull();
    }
}
