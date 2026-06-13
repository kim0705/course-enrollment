package com.yujin.course_enrollment.controller;

import com.yujin.course_enrollment.dto.req.ReqAdminCoursePageDto;
import com.yujin.course_enrollment.dto.req.ReqAdminPaymentPageDto;
import com.yujin.course_enrollment.dto.req.ReqAdminRoleUpdateDto;
import com.yujin.course_enrollment.dto.req.ReqUpdatePasswordDto;
import com.yujin.course_enrollment.dto.resp.RespAdminDashboardDto;
import com.yujin.course_enrollment.dto.resp.RespAdminPaymentDto;
import com.yujin.course_enrollment.dto.resp.RespCourseListDto;
import com.yujin.course_enrollment.dto.resp.RespPageDto;
import com.yujin.course_enrollment.entity.User;
import com.yujin.course_enrollment.global.response.ApiResponse;
import com.yujin.course_enrollment.service.AdminService;
import com.yujin.course_enrollment.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
    private final AdminService adminService;

    /**
     * 대시보드 통계 조회
     * GET /api/admin/dashboard
     * @return 전체 사용자 수, 강의 수, 확정 수강 신청 수
     */
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<RespAdminDashboardDto>> getDashboard() {
        log.debug("[AdminController] 대시보드 통계 조회 요청");

        return ResponseEntity.ok(ApiResponse.success(adminService.getDashboardStats()));
    }

    /**
     * 전체 사용자 목록 조회
     * GET /api/admin/users
     * @return 전체 사용자 목록
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<User>>> getUserList() {
        log.debug("[AdminController] 전체 사용자 목록 조회 요청");

        return ResponseEntity.ok(ApiResponse.success(adminService.findAllUsers()));
    }

    /**
     * 사용자 역할 변경
     * PATCH /api/admin/users/{userId}/role
     * @param userId 대상 사용자 ID
     * @param reqAdminRoleUpdateDto 변경할 역할
     * @return 200 OK
     */
    @PatchMapping("/users/{userId}/role")
    public ResponseEntity<ApiResponse<Void>> updateUserRole(@PathVariable Long userId, @Valid @RequestBody ReqAdminRoleUpdateDto reqAdminRoleUpdateDto) {
        log.debug("[AdminController] 사용자 역할 변경 - userId: {}, role: {}", userId, reqAdminRoleUpdateDto.getRole());

        adminService.updateUserRole(userId, reqAdminRoleUpdateDto.getRole());

        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 전체 강의 목록 조회
     * GET /api/admin/courses
     * @param reqAdminCoursePageDto 페이징 조건
     * @return 전체 강의 목록 (모든 상태 포함)
     */
    @GetMapping("/courses")
    public ResponseEntity<ApiResponse<RespPageDto<RespCourseListDto>>> getCourseList(ReqAdminCoursePageDto reqAdminCoursePageDto) {
        log.debug("[AdminController] 전체 강의 목록 조회 요청");

        return ResponseEntity.ok(ApiResponse.success(adminService.findAllCourses(reqAdminCoursePageDto)));
    }

    /**
     * 강의 강제 폐강
     * PATCH /api/admin/courses/{courseId}/close
     * @param courseId 강의 ID
     * @return 200 OK
     */
    @PatchMapping("/courses/{courseId}/close")
    public ResponseEntity<ApiResponse<Void>> forceCloseCourse(@PathVariable Long courseId) {
        log.debug("[AdminController] 강의 강제 폐강 요청 - courseId: {}", courseId);

        adminService.forceCloseCourse(courseId);

        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 전체 결제 내역 조회
     * GET /api/admin/payments
     * @param reqAdminPaymentPageDto 페이징 조건 (status 필터 선택)
     * @return 전체 결제 내역 (DONE, CANCELLED)
     */
    @GetMapping("/payments")
    public ResponseEntity<ApiResponse<RespPageDto<RespAdminPaymentDto>>> getPaymentList(ReqAdminPaymentPageDto reqAdminPaymentPageDto) {
        log.debug("[AdminController] 전체 결제 내역 조회 요청");

        return ResponseEntity.ok(ApiResponse.success(adminService.findAdminPayments(reqAdminPaymentPageDto)));
    }

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
