package com.yujin.course_enrollment.controller;

import com.yujin.course_enrollment.entity.User;
import com.yujin.course_enrollment.global.response.ApiResponse;
import com.yujin.course_enrollment.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 사용자 컨트롤러
 * 사용자 관련 HTTP 요청을 처리
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 전체 사용자 목록 조회
     * GET /api/users
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getUserList() {
        log.debug("[UserController] 전체 사용자 목록 조회 요청");

        List<User> users = userService.findUserList();

        return ResponseEntity.ok(ApiResponse.success(users));
    }
}
