package com.yujin.course_enrollment.controller;

import com.yujin.course_enrollment.dto.req.ReqEnrollmentCreateDto;
import com.yujin.course_enrollment.dto.resp.RespEnrollmentDto;
import com.yujin.course_enrollment.global.response.ApiResponse;
import com.yujin.course_enrollment.service.EnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ApiResponse<RespEnrollmentDto>> createEnrollment(@RequestHeader("X-User-Id") Long userId, @Valid @RequestBody ReqEnrollmentCreateDto reqEnrollmentCreateDto) {
        log.debug("[EnrollmentController] 수강 신청 요청 - userId: {}, courseId: {}", userId, reqEnrollmentCreateDto.getCourseId());

        RespEnrollmentDto result = enrollmentService.registerEnrollment(userId, reqEnrollmentCreateDto);

        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
