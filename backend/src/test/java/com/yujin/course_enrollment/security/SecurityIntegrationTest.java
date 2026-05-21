package com.yujin.course_enrollment.security;

import com.yujin.course_enrollment.config.JwtUtil;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Spring Security 통합 테스트
 * JWT 인증/인가 필터 및 접근 제어 규칙 검증
 */
@SpringBootTest(properties = "jwt.secret=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @MockitoBean
    private StringRedisTemplate stringRedisTemplate;

    @Test
    @DisplayName("토큰 없음 - 보호된 엔드포인트 접근 시 401")
    void noToken_protectedEndpoint_returns401() throws Exception {
        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("유효하지 않은 토큰 - 보호된 엔드포인트 접근 시 401")
    void invalidToken_protectedEndpoint_returns401() throws Exception {
        mockMvc.perform(post("/api/courses")
                        .cookie(new Cookie("accessToken", "invalid.jwt.token"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("STUDENT 역할로 강사 전용 엔드포인트 접근 - 403")
    void studentRole_creatorEndpoint_returns403() throws Exception {
        String token = jwtUtil.generateToken(1L, "STUDENT");

        mockMvc.perform(post("/api/courses")
                        .cookie(new Cookie("accessToken", token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("CREATOR 토큰으로 강사 전용 엔드포인트 접근 - Security 통과 (401/403 아님)")
    void creatorToken_creatorEndpoint_passesSecurity() throws Exception {
        String token = jwtUtil.generateToken(1L, "CREATOR");

        mockMvc.perform(post("/api/courses")
                        .cookie(new Cookie("accessToken", token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(result ->
                        assertThat(result.getResponse().getStatus())
                                .isNotEqualTo(401)
                                .isNotEqualTo(403));
    }

    @Test
    @DisplayName("ADMIN 역할로 수강생 전용 엔드포인트 접근 - 403")
    void adminRole_studentEndpoint_returns403() throws Exception {
        String token = jwtUtil.generateToken(1L, "ADMIN");

        mockMvc.perform(get("/api/enrollments")
                        .cookie(new Cookie("accessToken", token)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("인증된 사용자가 회원가입 시도 - 403")
    void authenticatedUser_signup_returns403() throws Exception {
        String token = jwtUtil.generateToken(1L, "STUDENT");

        mockMvc.perform(post("/api/auth/signup")
                        .cookie(new Cookie("accessToken", token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"newuser\",\"password\":\"pass\",\"name\":\"테스트\",\"email\":\"test@test.com\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("블랙리스트 토큰 - 보호된 엔드포인트 접근 시 401")
    void blacklistedToken_protectedEndpoint_returns401() throws Exception {
        // given
        String token = jwtUtil.generateToken(1L, "STUDENT");
        given(stringRedisTemplate.hasKey("BL:" + token)).willReturn(true);

        // when & then
        mockMvc.perform(get("/api/enrollments")
                        .cookie(new Cookie("accessToken", token)))
                .andExpect(status().isUnauthorized());
    }
}
