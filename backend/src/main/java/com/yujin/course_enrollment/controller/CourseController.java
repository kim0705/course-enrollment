package com.yujin.course_enrollment.controller;

import com.yujin.course_enrollment.dto.req.ReqCourseCreateDto;
import com.yujin.course_enrollment.dto.req.ReqCourseSearchDto;
import com.yujin.course_enrollment.dto.req.ReqCourseUpdateDto;
import com.yujin.course_enrollment.dto.resp.RespCourseCreateDto;
import com.yujin.course_enrollment.dto.resp.RespCourseDetailDto;
import com.yujin.course_enrollment.dto.resp.RespCourseListDto;
import com.yujin.course_enrollment.dto.resp.RespPageDto;
import com.yujin.course_enrollment.global.response.ApiResponse;
import com.yujin.course_enrollment.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
     * @param courseId 강의 ID
     */
    @GetMapping("/{courseId}")
    public ResponseEntity<ApiResponse<RespCourseDetailDto>> getCourseDetail(@PathVariable Long courseId) {
        log.debug("[CourseController] 강의 상세 조회 요청 - courseId: {}", courseId);

        RespCourseDetailDto result = courseService.findCourseById(courseId);

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
}