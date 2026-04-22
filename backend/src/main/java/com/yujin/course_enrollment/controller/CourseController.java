package com.yujin.course_enrollment.controller;

import com.yujin.course_enrollment.dto.req.ReqCourseCreateDto;
import com.yujin.course_enrollment.dto.resp.RespCourseCreateDto;
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
}