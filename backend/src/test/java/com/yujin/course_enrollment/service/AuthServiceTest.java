package com.yujin.course_enrollment.service;

import com.yujin.course_enrollment.config.JwtUtil;
import com.yujin.course_enrollment.entity.User;
import com.yujin.course_enrollment.global.exception.BusinessException;
import com.yujin.course_enrollment.mapper.UserMapper;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Date;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

/**
 * 인증 서비스 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Test
    @DisplayName("로그인 성공")
    void login_success() {
        // given
        User user = User.builder().id(1L).username("user1").password("encoded").role("STUDENT").build();
        given(userMapper.selectUserByUsername("user1")).willReturn(user);
        given(passwordEncoder.matches("raw", "encoded")).willReturn(true);

        // when
        User result = authService.login("user1", "raw");

        // then
        assertThat(result.getUsername()).isEqualTo("user1");
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 아이디")
    void login_userNotFound() {
        // given
        given(userMapper.selectUserByUsername("unknown")).willReturn(null);

        // when & then
        assertThatThrownBy(() -> authService.login("unknown", "any"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("아이디 또는 비밀번호가 올바르지 않습니다.");
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void login_wrongPassword() {
        // given
        User user = User.builder().id(1L).username("user1").password("encoded").build();
        given(userMapper.selectUserByUsername("user1")).willReturn(user);
        given(passwordEncoder.matches("wrong", "encoded")).willReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.login("user1", "wrong"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("아이디 또는 비밀번호가 올바르지 않습니다.");
    }

    @Test
    @DisplayName("아이디 사용 가능 - 미등록 아이디")
    void isUsernameAvailable_true() {
        // given
        given(userMapper.selectUserByUsername("newuser")).willReturn(null);

        // when & then
        assertThat(authService.isUsernameAvailable("newuser")).isTrue();
    }

    @Test
    @DisplayName("아이디 사용 불가 - 이미 등록된 아이디")
    void isUsernameAvailable_false() {
        // given
        given(userMapper.selectUserByUsername("existing")).willReturn(User.builder().build());

        // when & then
        assertThat(authService.isUsernameAvailable("existing")).isFalse();
    }

    @Test
    @DisplayName("이메일 사용 가능 - 미등록 이메일")
    void isEmailAvailable_true() {
        // given
        given(userMapper.selectUserByEmail("new@test.com")).willReturn(null);

        // when & then
        assertThat(authService.isEmailAvailable("new@test.com")).isTrue();
    }

    @Test
    @DisplayName("이메일 사용 불가 - 이미 등록된 이메일")
    void isEmailAvailable_false() {
        // given
        given(userMapper.selectUserByEmail("dup@test.com")).willReturn(User.builder().build());

        // when & then
        assertThat(authService.isEmailAvailable("dup@test.com")).isFalse();
    }

    @Test
    @DisplayName("회원가입 성공")
    void signup_success() {
        // given
        User newUser = User.builder().username("newuser").name("이름").email("new@test.com").password("raw").build();
        given(userMapper.selectUserByUsername("newuser")).willReturn(null);
        given(userMapper.selectUserByEmail("new@test.com")).willReturn(null);
        given(passwordEncoder.encode("raw")).willReturn("encoded");
        willDoNothing().given(userMapper).insertUser(any());

        // when & then
        assertThatNoException().isThrownBy(() -> authService.signup(newUser));
        then(userMapper).should().insertUser(any());
    }

    @Test
    @DisplayName("회원가입 실패 - 아이디 중복")
    void signup_usernameConflict() {
        // given
        User newUser = User.builder().username("dup").name("이름").email("new@test.com").password("raw").build();
        given(userMapper.selectUserByUsername("dup")).willReturn(User.builder().build());

        // when & then
        assertThatThrownBy(() -> authService.signup(newUser))
                .isInstanceOf(BusinessException.class)
                .hasMessage("이미 사용 중인 아이디입니다.");
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void signup_emailConflict() {
        // given
        User newUser = User.builder().username("newuser").name("이름").email("dup@test.com").password("raw").build();
        given(userMapper.selectUserByUsername("newuser")).willReturn(null);
        given(userMapper.selectUserByEmail("dup@test.com")).willReturn(User.builder().build());

        // when & then
        assertThatThrownBy(() -> authService.signup(newUser))
                .isInstanceOf(BusinessException.class)
                .hasMessage("이미 사용 중인 이메일입니다.");
    }

    @Test
    @DisplayName("로그아웃 - refreshToken 삭제")
    void deleteRefreshToken_withToken() {
        // when
        authService.deleteRefreshToken("some-token");

        // then
        then(refreshTokenService).should().delete("some-token");
    }

    @Test
    @DisplayName("로그아웃 - refreshToken null이면 삭제 생략")
    void deleteRefreshToken_withNull() {
        // when
        authService.deleteRefreshToken(null);

        // then
        then(refreshTokenService).should(never()).delete(any());
    }

    @Test
    @DisplayName("AccessToken 블랙리스트 등록 - 유효한 토큰")
    void blacklistAccessToken_validToken() {
        // given
        Date expiration = new Date(System.currentTimeMillis() + 60_000);
        given(jwtUtil.getExpiration("valid-token")).willReturn(expiration);

        // when
        authService.blacklistAccessToken("valid-token");

        // then
        then(tokenBlacklistService).should().add("valid-token", expiration);
    }

    @Test
    @DisplayName("AccessToken 블랙리스트 등록 - 만료된 토큰은 생략")
    void blacklistAccessToken_expiredToken_skips() {
        // given
        given(jwtUtil.getExpiration("expired-token")).willThrow(new JwtException("expired"));

        // when
        authService.blacklistAccessToken("expired-token");

        // then
        then(tokenBlacklistService).should(never()).add(any(), any());
    }

    @Test
    @DisplayName("AccessToken 블랙리스트 등록 - null이면 생략")
    void blacklistAccessToken_null_skips() {
        // when
        authService.blacklistAccessToken(null);

        // then
        then(tokenBlacklistService).should(never()).add(any(), any());
    }
}
