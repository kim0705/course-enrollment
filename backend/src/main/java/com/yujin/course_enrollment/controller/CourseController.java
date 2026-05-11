package com.yujin.course_enrollment.controller;

import com.yujin.course_enrollment.dto.req.ReqCourseCreateDto;
import com.yujin.course_enrollment.dto.req.ReqCourseEnrollmentPageDto;
import com.yujin.course_enrollment.dto.req.ReqCourseSearchDto;
import com.yujin.course_enrollment.dto.req.ReqCourseUpdateDto;
import com.yujin.course_enrollment.dto.req.ReqMyCoursePageDto;
import com.yujin.course_enrollment.dto.resp.RespCourseCreateDto;
import com.yujin.course_enrollment.dto.resp.RespCourseDetailDto;
import com.yujin.course_enrollment.dto.resp.RespCourseListDto;
import com.yujin.course_enrollment.dto.resp.RespEnrollmentCreatorDto;
import com.yujin.course_enrollment.dto.resp.RespPageDto;
import com.yujin.course_enrollment.global.response.ApiResponse;
import com.yujin.course_enrollment.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * 강의 컨트롤러
 * 강의 등록, 수정, 조회 등 강의 관련 HTTP 요청을 처리
 */
@Slf4j
@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    /**
     * 강의 등록
     * POST /api/courses
     * @param creatorId 크리에이터 ID (헤더로 전달)
     * @param reqCourseCreateDto 강의 등록 요청 DTO
     */
    @PostMapping
    public ResponseEntity<ApiResponse<RespCourseCreateDto>> createCourse(@RequestHeader("X-User-Id") Long creatorId, @Valid @RequestBody ReqCourseCreateDto reqCourseCreateDto) {
        log.debug("[CourseController] 강의 등록 요청 - creatorId: {}, title: {}", creatorId, reqCourseCreateDto.getTitle());

        RespCourseCreateDto respCourseCreateDto = courseService.registerCourse(creatorId, reqCourseCreateDto);

        return ResponseEntity.ok(ApiResponse.success(respCourseCreateDto));
    }

    /**
     * 강의 목록 조회
     * GET /api/courses
     * @param reqCourseSearchDto 검색 조건 DTO
     */
    @GetMapping
    public ResponseEntity<ApiResponse<RespPageDto<RespCourseListDto>>> getCourseList(ReqCourseSearchDto reqCourseSearchDto) {
        log.debug("[CourseController] 강의 목록 조회 요청 - status: {}, keyword: {}", reqCourseSearchDto.getStatus(), reqCourseSearchDto.getKeyword());

        RespPageDto<RespCourseListDto> result = courseService.findCourseList(reqCourseSearchDto);

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 강의 상세 조회
     * GET /api/courses/{courseId}
     * @param userId 사용자 ID (헤더로 전달, 수강 신청 여부 확인용)
     * @param courseId 강의 ID
     */
    @GetMapping("/{courseId}")
    public ResponseEntity<ApiResponse<RespCourseDetailDto>> getCourseDetail(@RequestHeader(value = "X-User-Id", required = false) Long userId, @PathVariable Long courseId) {
        log.debug("[CourseController] 강의 상세 조회 요청 - courseId: {}", courseId);

        RespCourseDetailDto result = courseService.findCourseById(courseId, userId);

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 강의 수정
     * PUT /api/courses/{courseId}
     * @param creatorId 크리에이터 ID (헤더로 전달)
     * @param courseId 강의 ID
     * @param reqCourseUpdateDto 강의 수정 요청 DTO
     */
    @PutMapping("/{courseId}")
    public ResponseEntity<ApiResponse<RespCourseDetailDto>> updateCourse(@RequestHeader("X-User-Id") Long creatorId, @PathVariable Long courseId, @Valid @RequestBody ReqCourseUpdateDto reqCourseUpdateDto) {
        log.debug("[CourseController] 강의 수정 요청 - creatorId: {}, courseId: {}", creatorId, courseId);

        RespCourseDetailDto result = courseService.modifyCourse(creatorId, courseId, reqCourseUpdateDto);

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 강의 공개 (DRAFT → OPEN)
     * PATCH /api/courses/{courseId}/publish
     * @param creatorId 크리에이터 ID (헤더로 전달)
     * @param courseId 강의 ID
     */
    @PatchMapping("/{courseId}/publish")
    public ResponseEntity<ApiResponse<RespCourseDetailDto>> publishCourse(@RequestHeader("X-User-Id") Long creatorId, @PathVariable Long courseId) {
        log.debug("[CourseController] 강의 공개 요청 - creatorId: {}, courseId: {}", creatorId, courseId);

        RespCourseDetailDto result = courseService.publishCourse(creatorId, courseId);

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 강의 마감 (OPEN → CLOSED)
     * PATCH /api/courses/{courseId}/close
     * @param creatorId 크리에이터 ID (헤더로 전달)
     * @param courseId 강의 ID
     */
    @PatchMapping("/{courseId}/close")
    public ResponseEntity<ApiResponse<RespCourseDetailDto>> closeCourse(@RequestHeader("X-User-Id") Long creatorId, @PathVariable Long courseId) {
        log.debug("[CourseController] 강의 마감 요청 - creatorId: {}, courseId: {}", creatorId, courseId);

        RespCourseDetailDto result = courseService.closeCourse(creatorId, courseId);

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 나의 강의 목록 조회 (CREATOR 전용)
     * GET /api/courses/my
     * @param creatorId 크리에이터 ID (헤더로 전달)
     * @param reqMyCoursePageDto 페이징 조건 DTO
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<RespPageDto<RespCourseListDto>>> getMyCourses(@RequestHeader("X-User-Id") Long creatorId, ReqMyCoursePageDto reqMyCoursePageDto) {
        log.debug("[CourseController] 나의 강의 목록 조회 요청 - creatorId: {}", creatorId);

        RespPageDto<RespCourseListDto> result = courseService.findMyCourses(creatorId, reqMyCoursePageDto);

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 강의별 수강생 목록 조회 (CREATOR 전용)
     * GET /api/courses/{courseId}/enrollments
     * @param creatorId 크리에이터 ID (헤더로 전달)
     * @param courseId 강의 ID
     * @param reqCourseEnrollmentPageDto 페이징 조건 DTO
     */
    @GetMapping("/{courseId}/enrollments")
    public ResponseEntity<ApiResponse<RespPageDto<RespEnrollmentCreatorDto>>> getCourseEnrollments(@RequestHeader("X-User-Id") Long creatorId, @PathVariable Long courseId, ReqCourseEnrollmentPageDto reqCourseEnrollmentPageDto) {
        log.debug("[CourseController] 강의별 수강생 목록 조회 요청 - creatorId: {}, courseId: {}", creatorId, courseId);

        RespPageDto<RespEnrollmentCreatorDto> result = courseService.findCourseEnrollments(creatorId, courseId, reqCourseEnrollmentPageDto);

        return ResponseEntity.ok(ApiResponse.success(result));
    }
}