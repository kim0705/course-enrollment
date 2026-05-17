package com.yujin.course_enrollment.controller;

import com.yujin.course_enrollment.dto.req.ReqUpdatePasswordDto;
import com.yujin.course_enrollment.global.response.ApiResponse;
import com.yujin.course_enrollment.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 관리자 컨트롤러
 * 관리자 전용 HTTP 요청을 처리
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    /**
     * 관리자 비밀번호 변경
     * PATCH /api/admin/me/password
     * @param userId SecurityContext에서 추출된 관리자 ID
     * @param reqUpdatePasswordDto 현재 비밀번호·새 비밀번호
     * @return 200 OK
     */
    @PatchMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(@AuthenticationPrincipal Long userId, @Valid @RequestBody ReqUpdatePasswordDto reqUpdatePasswordDto) {
        log.debug("[AdminController] 비밀번호 변경 - userId: {}", userId);

        userService.updatePassword(userId, reqUpdatePasswordDto.getCurrentPassword(), reqUpdatePasswordDto.getNewPassword());

        return ResponseEntity.ok(ApiResponse.success());
    }
}
