package com.yujin.course_enrollment.service;

import com.yujin.course_enrollment.dto.req.ReqCourseCreateDto;
import com.yujin.course_enrollment.dto.resp.RespCourseCreateDto;
import com.yujin.course_enrollment.entity.Course;
import com.yujin.course_enrollment.entity.User;
import com.yujin.course_enrollment.mapper.CourseMapper;
import com.yujin.course_enrollment.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * 강의 서비스
 * 강의 등록, 수정, 조회 등 강의 관련 비즈니스 로직을 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseMapper courseMapper;
    private final UserMapper userMapper;

    /**
     * 강의 등록
     * @param creatorId 강의를 등록하는 크리에이터 ID
     * @param reqCourseCreateDto 강의 등록 요청 DTO
     */
    @Transactional
    public RespCourseCreateDto registerCourse(Long creatorId, ReqCourseCreateDto reqCourseCreateDto) {
        log.info("[CourseService] 강의 등록 - creatorId: {}, title: {}", creatorId, reqCourseCreateDto.getTitle());

        // 사용자 존재 여부 확인
        User user = userMapper.selectUserById(creatorId);
        if (user == null) {
            log.warn("[CourseService] 존재하지 않는 사용자 - creatorId: {}", creatorId);
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }

        // 크리에이터 권한 확인
        if (!"CREATOR".equals(user.getRole())) {
            log.warn("[CourseService] 크리에이터 권한 없음 - creatorId: {}, role: {}", creatorId, user.getRole());
            throw new IllegalArgumentException("강의 등록 권한이 없습니다.");
        }

        // 시작일 과거 검증
        if (reqCourseCreateDto.getStartDate().isBefore(LocalDate.now())) {
            log.warn("[CourseService] 시작일이 과거 - startDate: {}", reqCourseCreateDto.getStartDate());
            throw new IllegalArgumentException("시작일은 오늘 이후여야 합니다.");
        }

        // 종료일 과거 검증
        if (reqCourseCreateDto.getEndDate().isBefore(LocalDate.now())) {
            log.warn("[CourseService] 종료일이 과거 - endDate: {}", reqCourseCreateDto.getEndDate());
            throw new IllegalArgumentException("종료일은 오늘 이후여야 합니다.");
        }

        // 날짜 유효성 확인
        if (reqCourseCreateDto.getEndDate().isBefore(reqCourseCreateDto.getStartDate())) {
            log.warn("[CourseService] 날짜 유효성 오류 - startDate: {}, endDate: {}", reqCourseCreateDto.getStartDate(), reqCourseCreateDto.getEndDate());
            throw new IllegalArgumentException("종료일은 시작일보다 이전일 수 없습니다.");
        }

        Course course = reqCourseCreateDto.toEntity(creatorId);
        courseMapper.insertCourse(course);

        log.info("[CourseService] 강의 등록 완료 - courseId: {}", course.getId());

        Course savedCourse = courseMapper.selectCourseById(course.getId());
        if (savedCourse == null) {
            log.error("[CourseService] 강의 등록 후 조회 실패 - courseId: {}", course.getId());
            throw new IllegalStateException("강의 등록에 실패했습니다.");
        }

        return RespCourseCreateDto.from(savedCourse);
    }
}