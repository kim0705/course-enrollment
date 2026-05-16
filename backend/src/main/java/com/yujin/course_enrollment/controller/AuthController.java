package com.yujin.course_enrollment.controller;

import com.yujin.course_enrollment.dto.req.ReqLoginDto;
import com.yujin.course_enrollment.dto.resp.RespLoginDto;
import com.yujin.course_enrollment.entity.User;
import com.yujin.course_enrollment.global.exception.BusinessException;
import com.yujin.course_enrollment.global.response.ApiResponse;
import com.yujin.course_enrollment.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Map;

/**
 * 인증 컨트롤러
 * 회원가입 등 인증 관련 HTTP 요청을 처리
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 로그인
     * POST /api/auth/login
     * @param reqLoginDto 로그인 요청 (username, password)
     * @param response HTTP 응답 (accessToken, refreshToken httpOnly 쿠키 설정)
     * @return 사용자 정보 (id, username, name, role)
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<RespLoginDto>> login(@Valid @RequestBody ReqLoginDto reqLoginDto, HttpServletResponse response) {
        log.debug("[AuthController] 로그인 요청 - username: {}", reqLoginDto.getUsername());

        User user = authService.login(reqLoginDto.getUsername(), reqLoginDto.getPassword());
        String accessToken = authService.generateAccessToken(user);
        String refreshToken = authService.generateRefreshToken(user.getId());

        response.addHeader(HttpHeaders.SET_COOKIE, buildAccessTokenCookie(accessToken).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, buildRefreshTokenCookie(refreshToken).toString());

        return ResponseEntity.ok(ApiResponse.success(RespLoginDto.from(user)));
    }

    /**
     * 로그아웃
     * POST /api/auth/logout
     * @param request HTTP 요청 (refreshToken 쿠키 추출)
     * @param response HTTP 응답 (쿠키 만료 처리)
     * @return 200 OK
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request, HttpServletResponse response) {
        log.debug("[AuthController] 로그아웃 요청");

        authService.deleteRefreshToken(extractRefreshToken(request));

        response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie("accessToken").toString());
        response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie("refreshToken").toString());

        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * AccessToken 재발급
     * POST /api/auth/refresh
     * @param request HTTP 요청 (refreshToken 쿠키 추출)
     * @param response HTTP 응답 (새 accessToken, refreshToken 쿠키 설정)
     * @return 200 OK
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Void>> refresh(HttpServletRequest request, HttpServletResponse response) {
        log.debug("[AuthController] 토큰 재발급 요청");

        String refreshToken = extractRefreshToken(request);
        if (refreshToken == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "refreshToken이 없습니다.");
        }

        User user = authService.validateAndRotateRefreshToken(refreshToken);
        String newAccessToken = authService.generateAccessToken(user);
        String newRefreshToken = authService.generateRefreshToken(user.getId());

        response.addHeader(HttpHeaders.SET_COOKIE, buildAccessTokenCookie(newAccessToken).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, buildRefreshTokenCookie(newRefreshToken).toString());

        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 아이디 중복 확인
     * GET /api/auth/check-username?value=xxx
     * @param value 확인할 아이디
     * @return { available: true/false }
     */
    @GetMapping("/check-username")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkUsername(@RequestParam String value) {
        log.debug("[AuthController] 아이디 중복 확인 - username: {}", value);

        return ResponseEntity.ok(ApiResponse.success(Map.of("available", authService.isUsernameAvailable(value))));
    }

    /**
     * 이메일 중복 확인
     * GET /api/auth/check-email?value=xxx
     * @param value 확인할 이메일
     * @return { available: true/false }
     */
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkEmail(@RequestParam String value) {
        log.debug("[AuthController] 이메일 중복 확인 - email: {}", value);

        return ResponseEntity.ok(ApiResponse.success(Map.of("available", authService.isEmailAvailable(value))));
    }

    /**
     * 회원가입
     * POST /api/auth/signup
     * @param user 회원가입 요청 (username, name, email, password)
     * @return 201 Created
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestBody User user) {
        log.debug("[AuthController] 회원가입 요청 - username: {}", user.getUsername());

        authService.signup(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created());
    }

    /**
     * accessToken httpOnly 쿠키 생성 (15분 유효)
     * @param value 쿠키에 담을 토큰 값
     * @return Set-Cookie 헤더용 ResponseCookie
     */
    private ResponseCookie buildAccessTokenCookie(String value) {
        return ResponseCookie.from("accessToken", value)
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofMinutes(15))
                .sameSite("Lax")
                .build();
    }

    /**
     * refreshToken httpOnly 쿠키 생성 (7일 유효)
     * @param value 쿠키에 담을 토큰 값
     * @return Set-Cookie 헤더용 ResponseCookie
     */
    private ResponseCookie buildRefreshTokenCookie(String value) {
        return ResponseCookie.from("refreshToken", value)
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofDays(7))
                .sameSite("Lax")
                .build();
    }

    /**
     * 만료된 쿠키 생성 (로그아웃 시 기존 쿠키 제거용)
     * @param name 쿠키 이름
     * @return maxAge=0인 ResponseCookie
     */
    private ResponseCookie expiredCookie(String name) {
        return ResponseCookie.from(name, "")
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ZERO)
                .sameSite("Lax")
                .build();
    }

    /**
     * 요청 쿠키에서 refreshToken 추출
     * @param request HTTP 요청
     * @return refreshToken 값, 없으면 null
     */
    private String extractRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) return null;

        for (Cookie cookie : request.getCookies()) {
            if ("refreshToken".equals(cookie.getName())) return cookie.getValue();
        }

        return null;
    }
}
