package com.yujin.course_enrollment.controller;

import com.yujin.course_enrollment.dto.req.ReqEnrollmentCancelDto;
import com.yujin.course_enrollment.dto.req.ReqEnrollmentCreateDto;
import com.yujin.course_enrollment.dto.req.ReqEnrollmentPageDto;
import com.yujin.course_enrollment.dto.resp.RespEnrollmentDto;
import com.yujin.course_enrollment.dto.resp.RespEnrollmentStudentDto;
import com.yujin.course_enrollment.dto.resp.RespPageDto;
import com.yujin.course_enrollment.global.response.ApiResponse;
import com.yujin.course_enrollment.service.EnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 수강 신청 컨트롤러
 * 수강 신청 등록 등 수강 신청 관련 HTTP 요청을 처리
 */
@Slf4j
@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    /**
     * 수강 신청
     * POST /api/enrollments
     * @param userId 사용자 ID (헤더로 전달)
     * @param reqEnrollmentCreateDto 수강 신청 요청 DTO
     */
    @PostMapping
    public ResponseEntity<ApiResponse<RespEnrollmentDto>> createEnrollment(@AuthenticationPrincipal Long userId, @Valid @RequestBody ReqEnrollmentCreateDto reqEnrollmentCreateDto) {
        log.debug("[EnrollmentController] 수강 신청 요청 - userId: {}, courseId: {}", userId, reqEnrollmentCreateDto.getCourseId());

        RespEnrollmentDto result = enrollmentService.registerEnrollment(userId, reqEnrollmentCreateDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(result));
    }

    /**
     * 나의 수강 신청 목록 조회
     * GET /api/enrollments/me?page=0&size=5
     * @param userId 사용자 ID (헤더로 전달)
     * @param reqEnrollmentPageDto 페이징 조건 (page, size)
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<RespPageDto<RespEnrollmentStudentDto>>> getMyEnrollments(@AuthenticationPrincipal Long userId, ReqEnrollmentPageDto reqEnrollmentPageDto) {
        log.debug("[EnrollmentController] 나의 수강 신청 목록 조회 요청 - userId: {}", userId);

        RespPageDto<RespEnrollmentStudentDto> result = enrollmentService.findMyEnrollments(userId, reqEnrollmentPageDto);

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 결제 요청 (PENDING → CONFIRMED)
     * PATCH /api/enrollments/{enrollmentId}/confirm
     * @param userId 사용자 ID (헤더로 전달)
     * @param enrollmentId 수강 신청 ID
     */
    @PatchMapping("/{enrollmentId}/confirm")
    public ResponseEntity<ApiResponse<RespEnrollmentDto>> confirmEnrollment(@AuthenticationPrincipal Long userId, @PathVariable Long enrollmentId) {
        log.debug("[EnrollmentController] 결제 요청 - userId: {}, enrollmentId: {}", userId, enrollmentId);

        RespEnrollmentDto result = enrollmentService.confirmEnrollment(userId, enrollmentId);

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 수강 취소 (PENDING, CONFIRMED, WAITLIST → CANCELLED)
     * PATCH /api/enrollments/{enrollmentId}/cancel
     * @param userId 사용자 ID (헤더로 전달)
     * @param enrollmentId 수강 신청 ID
     * @param reqEnrollmentCancelDto 취소 요청 DTO (CONFIRMED 취소 시 cancelReason 필수)
     */
    @PatchMapping("/{enrollmentId}/cancel")
    public ResponseEntity<ApiResponse<RespEnrollmentDto>> cancelEnrollment(@AuthenticationPrincipal Long userId, @PathVariable Long enrollmentId, @RequestBody(required = false) ReqEnrollmentCancelDto reqEnrollmentCancelDto) {
        log.debug("[EnrollmentController] 수강 취소 요청 - userId: {}, enrollmentId: {}", userId, enrollmentId);

        String cancelReason = reqEnrollmentCancelDto != null ? reqEnrollmentCancelDto.getCancelReason() : null;
        RespEnrollmentDto result = enrollmentService.cancelEnrollment(userId, enrollmentId, cancelReason);

        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
