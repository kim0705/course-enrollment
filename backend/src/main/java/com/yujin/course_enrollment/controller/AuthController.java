package com.yujin.course_enrollment.controller;

import com.yujin.course_enrollment.dto.req.ReqLoginDto;
import com.yujin.course_enrollment.dto.resp.RespLoginDto;
import com.yujin.course_enrollment.entity.User;
import com.yujin.course_enrollment.global.response.ApiResponse;
import com.yujin.course_enrollment.service.AuthService;
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
     * @param response HTTP 응답 (accessToken httpOnly 쿠키 설정)
     * @return 사용자 정보 (id, username, name, role)
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<RespLoginDto>> login(@Valid @RequestBody ReqLoginDto reqLoginDto, HttpServletResponse response) {
        log.debug("[AuthController] 로그인 요청 - username: {}", reqLoginDto.getUsername());

        User user = authService.login(reqLoginDto.getUsername(), reqLoginDto.getPassword());
        String token = authService.generateToken(user);

        ResponseCookie cookie = ResponseCookie.from("accessToken", token)
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofMinutes(15))
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(ApiResponse.success(RespLoginDto.from(user)));
    }

    /**
     * 로그아웃
     * POST /api/auth/logout
     * @param response HTTP 응답 (accessToken 쿠키 삭제)
     * @return 200 OK
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse response) {
        log.debug("[AuthController] 로그아웃 요청");

        ResponseCookie cookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ZERO)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

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
}
