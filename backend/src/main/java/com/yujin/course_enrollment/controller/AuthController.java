package com.yujin.course_enrollment.controller;

import com.yujin.course_enrollment.entity.User;
import com.yujin.course_enrollment.global.response.ApiResponse;
import com.yujin.course_enrollment.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    private final UserService userService;

    /**
     * 아이디 중복 확인
     * GET /api/auth/check-username?value=xxx
     * @param value 확인할 아이디
     * @return { available: true/false }
     */
    @GetMapping("/check-username")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkUsername(@RequestParam String value) {
        log.debug("[AuthController] 아이디 중복 확인 - username: {}", value);

        return ResponseEntity.ok(ApiResponse.success(Map.of("available", userService.isUsernameAvailable(value))));
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

        return ResponseEntity.ok(ApiResponse.success(Map.of("available", userService.isEmailAvailable(value))));
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

        userService.signup(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created());
    }
}
