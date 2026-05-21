package com.yujin.course_enrollment.controller;

import com.yujin.course_enrollment.dto.req.ReqUpdatePasswordDto;
import com.yujin.course_enrollment.dto.req.ReqUpdateProfileDto;
import com.yujin.course_enrollment.entity.User;
import com.yujin.course_enrollment.global.response.ApiResponse;
import com.yujin.course_enrollment.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

        return ResponseEntity.ok(ApiResponse.success(userService.findUserList()));
    }

    /**
     * 프로필 수정 (이름·이메일)
     * PATCH /api/users/me
     * @param userId SecurityContext에서 추출된 사용자 ID
     * @param reqUpdateProfileDto 이름·이메일
     * @return 200 OK
     */
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<Void>> updateProfile(@AuthenticationPrincipal Long userId, @Valid @RequestBody ReqUpdateProfileDto reqUpdateProfileDto) {
        log.debug("[UserController] 프로필 수정 - userId: {}", userId);

        userService.updateProfile(userId, reqUpdateProfileDto.getName(), reqUpdateProfileDto.getEmail());

        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 비밀번호 변경 (STUDENT / CREATOR)
     * PATCH /api/users/me/password
     * @param userId SecurityContext에서 추출된 사용자 ID
     * @param reqUpdatePasswordDto 현재 비밀번호·새 비밀번호
     * @return 200 OK
     */
    @PatchMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(@AuthenticationPrincipal Long userId, @Valid @RequestBody ReqUpdatePasswordDto reqUpdatePasswordDto) {
        log.debug("[UserController] 비밀번호 변경 - userId: {}", userId);

        userService.updatePassword(userId, reqUpdatePasswordDto.getCurrentPassword(), reqUpdatePasswordDto.getNewPassword());

        return ResponseEntity.ok(ApiResponse.success());
    }
}
