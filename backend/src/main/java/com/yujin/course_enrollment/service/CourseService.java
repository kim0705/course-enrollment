package com.yujin.course_enrollment.service;

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
import com.yujin.course_enrollment.entity.Course;
import com.yujin.course_enrollment.entity.User;
import com.yujin.course_enrollment.global.CourseStatus;
import com.yujin.course_enrollment.global.EnrollmentStatus;
import com.yujin.course_enrollment.global.exception.BusinessException;
import com.yujin.course_enrollment.entity.Enrollment;
import com.yujin.course_enrollment.mapper.CourseMapper;
import com.yujin.course_enrollment.mapper.EnrollmentMapper;
import com.yujin.course_enrollment.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 강의 서비스
 * 강의 등록, 수정, 조회 등 강의 관련 비즈니스 로직을 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {

    private final CourseMapper courseMapper;
    private final UserMapper userMapper;
    private final EnrollmentMapper enrollmentMapper;

    /**
     * 강의 등록
     * @param creatorId 강의를 등록하는 크리에이터 ID
     * @param reqCourseCreateDto 강의 등록 요청 DTO
     * @return 등록된 강의 응답 DTO
     * @throws BusinessException 사용자 없음(400), 등록 권한 없음(403), 날짜 유효성 오류(400), 등록 실패(500)
     */
    @Transactional
    public RespCourseCreateDto registerCourse(Long creatorId, ReqCourseCreateDto reqCourseCreateDto) {
        log.info("[CourseService] 강의 등록 - creatorId: {}, title: {}", creatorId, reqCourseCreateDto.getTitle());

        // 사용자 존재 여부 확인
        User user = userMapper.selectUserById(creatorId);
        if (user == null) {
            log.warn("[CourseService] 존재하지 않는 사용자 - creatorId: {}", creatorId);
            throw new BusinessException(HttpStatus.BAD_REQUEST, "존재하지 않는 사용자입니다.");
        }

        // 크리에이터 권한 확인
        if (!"CREATOR".equals(user.getRole())) {
            log.warn("[CourseService] 크리에이터 권한 없음 - creatorId: {}, role: {}", creatorId, user.getRole());
            throw new BusinessException(HttpStatus.FORBIDDEN, "강의 등록 권한이 없습니다.");
        }

        // 시작일 과거 검증
        if (reqCourseCreateDto.getStartDate().isBefore(LocalDate.now())) {
            log.warn("[CourseService] 시작일이 과거 - startDate: {}", reqCourseCreateDto.getStartDate());
            throw new BusinessException(HttpStatus.BAD_REQUEST, "시작일은 오늘 이후여야 합니다.");
        }

        // 종료일 과거 검증
        if (reqCourseCreateDto.getEndDate().isBefore(LocalDate.now())) {
            log.warn("[CourseService] 종료일이 과거 - endDate: {}", reqCourseCreateDto.getEndDate());
            throw new BusinessException(HttpStatus.BAD_REQUEST, "종료일은 오늘 이후여야 합니다.");
        }

        // 날짜 유효성 확인
        if (reqCourseCreateDto.getEndDate().isBefore(reqCourseCreateDto.getStartDate())) {
            log.warn("[CourseService] 날짜 유효성 오류 - startDate: {}, endDate: {}", reqCourseCreateDto.getStartDate(), reqCourseCreateDto.getEndDate());
            throw new BusinessException(HttpStatus.BAD_REQUEST, "종료일은 시작일보다 이전일 수 없습니다.");
        }

        Course course = reqCourseCreateDto.toEntity(creatorId);
        courseMapper.insertCourse(course);

        log.info("[CourseService] 강의 등록 완료 - courseId: {}", course.getId());

        Course savedCourse = courseMapper.selectCourseById(course.getId());
        if (savedCourse == null) {
            log.error("[CourseService] 강의 등록 후 조회 실패 - courseId: {}", course.getId());
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "강의 등록에 실패했습니다.");
        }

        return RespCourseCreateDto.from(savedCourse);
    }

    /**
     * 강의 목록 조회
     * @param reqCourseSearchDto 검색 조건 DTO
     * @return 페이징된 강의 목록
     */
    public RespPageDto<RespCourseListDto> findCourseList(ReqCourseSearchDto reqCourseSearchDto) {
        log.info("[CourseService] 강의 목록 조회 - status: {}, keyword: {}", reqCourseSearchDto.getStatus(), reqCourseSearchDto.getKeyword());

        List<RespCourseListDto> content = courseMapper.selectCourseList(reqCourseSearchDto);
        int totalCount = courseMapper.selectCourseListCount(reqCourseSearchDto);

        log.info("[CourseService] 강의 목록 조회 완료 - 총 {}건", totalCount);

        return RespPageDto.of(content, reqCourseSearchDto.getPage(), reqCourseSearchDto.getSize(), totalCount);
    }

    /**
     * 강의 상세 조회
     * @param courseId 강의 ID
     * @param userId 조회하는 사용자 ID (수강 신청 여부 확인용)
     * @return 강의 상세 응답 DTO
     * @throws BusinessException 강의 없음(400), DRAFT 강의 접근 권한 없음(403)
     */
    public RespCourseDetailDto findCourseById(Long courseId, Long userId) {
        log.info("[CourseService] 강의 상세 조회 - courseId: {}", courseId);

        // 강의 존재 여부 확인
        RespCourseDetailDto course = courseMapper.selectCourseDetailById(courseId);
        if (course == null) {
            log.warn("[CourseService] 강의 없음 - courseId: {}", courseId);
            throw new BusinessException(HttpStatus.BAD_REQUEST, "존재하지 않는 강의입니다.");
        }

        // 비공개 강의: 크리에이터 본인만 접근 가능
        if (CourseStatus.DRAFT.equals(course.getStatus()) && !course.getCreatorId().equals(userId)) {
            log.warn("[CourseService] DRAFT 강의 접근 차단 - courseId: {}, userId: {}", courseId, userId);
            throw new BusinessException(HttpStatus.FORBIDDEN, "접근할 수 없는 강의입니다.");
        }

        // 강제 폐강 강의: 외부 노출 차단
        if (CourseStatus.FORCE_CLOSED.equals(course.getStatus())) {
            log.warn("[CourseService] 강제 폐강 강의 접근 차단 - courseId: {}", courseId);
            throw new BusinessException(HttpStatus.NOT_FOUND, "존재하지 않는 강의입니다.");
        }

        // 수강 신청 여부 조회
        if (userId != null) {
            Enrollment enrollment = enrollmentMapper.selectEnrollmentByUserIdAndCourseId(userId, courseId);
            course.setEnrolled(enrollment != null && !EnrollmentStatus.CANCELLED.equals(enrollment.getStatus()));
        }

        log.info("[CourseService] 강의 상세 조회 완료 - courseId: {}", courseId);

        return course;
    }

    /**
     * 강의 수정
     * @param creatorId 크리에이터 ID
     * @param courseId 강의 ID
     * @param reqCourseUpdateDto 강의 수정 요청 DTO
     * @return 수정된 강의 상세 응답 DTO
     * @throws BusinessException 강의 없음(400), 수정 권한 없음(403), DRAFT 상태 아님(400), 날짜 유효성 오류(400)
     */
    @Transactional
    public RespCourseDetailDto modifyCourse(Long creatorId, Long courseId, ReqCourseUpdateDto reqCourseUpdateDto) {
        log.info("[CourseService] 강의 수정 - creatorId: {}, courseId: {}", creatorId, courseId);

        // 강의 존재 여부 확인
        Course course = courseMapper.selectCourseById(courseId);
        if (course == null) {
            log.warn("[CourseService] 강의 없음 - courseId: {}", courseId);
            throw new BusinessException(HttpStatus.BAD_REQUEST, "존재하지 않는 강의입니다.");
        }

        // 크리에이터 권한 확인
        if (!course.getCreatorId().equals(creatorId)) {
            log.warn("[CourseService] 수정 권한 없음 - creatorId: {}, courseId: {}", creatorId, courseId);
            throw new BusinessException(HttpStatus.FORBIDDEN, "강의 수정 권한이 없습니다.");
        }

        // DRAFT 상태에서만 수정 가능
        if (!CourseStatus.DRAFT.equals(course.getStatus())) {
            log.warn("[CourseService] DRAFT 상태가 아님 - courseId: {}, status: {}", courseId, course.getStatus());
            throw new BusinessException(HttpStatus.BAD_REQUEST, "DRAFT 상태의 강의만 수정할 수 있습니다.");
        }

        // 날짜 유효성 확인
        if (reqCourseUpdateDto.getEndDate().isBefore(reqCourseUpdateDto.getStartDate())) {
            log.warn("[CourseService] 날짜 유효성 오류 - startDate: {}, endDate: {}", reqCourseUpdateDto.getStartDate(), reqCourseUpdateDto.getEndDate());
            throw new BusinessException(HttpStatus.BAD_REQUEST, "종료일은 시작일보다 이전일 수 없습니다.");
        }

        courseMapper.updateCourse(reqCourseUpdateDto.toEntity(courseId, creatorId));

        log.info("[CourseService] 강의 수정 완료 - courseId: {}", courseId);

        return courseMapper.selectCourseDetailById(courseId);
    }

    /**
     * 강의 공개 (DRAFT → OPEN)
     * @param creatorId 크리에이터 ID
     * @param courseId 강의 ID
     * @return 공개된 강의 상세 응답 DTO
     * @throws BusinessException 강의 없음(400), 공개 권한 없음(403), DRAFT 상태 아님(400)
     */
    @Transactional
    public RespCourseDetailDto publishCourse(Long creatorId, Long courseId) {
        log.info("[CourseService] 강의 공개 - creatorId: {}, courseId: {}", creatorId, courseId);

        // 강의 존재 여부 확인
        Course course = courseMapper.selectCourseById(courseId);
        if (course == null) {
            log.warn("[CourseService] 강의 없음 - courseId: {}", courseId);
            throw new BusinessException(HttpStatus.BAD_REQUEST, "존재하지 않는 강의입니다.");
        }

        // 크리에이터 권한 확인
        if (!course.getCreatorId().equals(creatorId)) {
            log.warn("[CourseService] 권한 없음 - creatorId: {}, courseId: {}", creatorId, courseId);
            throw new BusinessException(HttpStatus.FORBIDDEN, "강의 공개 권한이 없습니다.");
        }

        // DRAFT 상태에서만 공개 가능
        if (!CourseStatus.DRAFT.equals(course.getStatus())) {
            log.warn("[CourseService] DRAFT 상태 아님 - courseId: {}, status: {}", courseId, course.getStatus());
            throw new BusinessException(HttpStatus.BAD_REQUEST, "DRAFT 상태의 강의만 공개할 수 있습니다.");
        }

        courseMapper.updateCourseStatus(Course.ofOpen(courseId));

        log.info("[CourseService] 강의 공개 완료 - courseId: {}", courseId);

        return courseMapper.selectCourseDetailById(courseId);
    }

    /**
     * 강의 마감 (OPEN → CLOSED)
     * @param creatorId 크리에이터 ID
     * @param courseId 강의 ID
     * @return 마감된 강의 상세 응답 DTO
     * @throws BusinessException 강의 없음(400), 마감 권한 없음(403), OPEN 상태 아님(400)
     */
    @Transactional
    public RespCourseDetailDto closeCourse(Long creatorId, Long courseId) {
        log.info("[CourseService] 강의 마감 - creatorId: {}, courseId: {}", creatorId, courseId);

        // 강의 존재 여부 확인
        Course course = courseMapper.selectCourseById(courseId);
        if (course == null) {
            log.warn("[CourseService] 강의 없음 - courseId: {}", courseId);
            throw new BusinessException(HttpStatus.BAD_REQUEST, "존재하지 않는 강의입니다.");
        }

        // 크리에이터 권한 확인
        if (!course.getCreatorId().equals(creatorId)) {
            log.warn("[CourseService] 권한 없음 - creatorId: {}, courseId: {}", creatorId, courseId);
            throw new BusinessException(HttpStatus.FORBIDDEN, "강의 마감 권한이 없습니다.");
        }

        // OPEN 상태에서만 마감 가능
        if (!CourseStatus.OPEN.equals(course.getStatus())) {
            log.warn("[CourseService] OPEN 상태 아님 - courseId: {}, status: {}", courseId, course.getStatus());
            throw new BusinessException(HttpStatus.BAD_REQUEST, "OPEN 상태의 강의만 마감할 수 있습니다.");
        }

        courseMapper.updateCourseStatus(Course.ofClosed(courseId));

        int cancelled = enrollmentMapper.updateWaitlistCancelledByCourseId(courseId);
        if (cancelled > 0) {
            log.info("[CourseService] 대기열 자동 취소 - courseId: {}, count: {}", courseId, cancelled);
        }

        log.info("[CourseService] 강의 마감 완료 - courseId: {}", courseId);

        return courseMapper.selectCourseDetailById(courseId);
    }

    /**
     * 강의 목록 조회 (CREATOR 전용)
     * @param creatorId 크리에이터 ID
     * @param reqMyCoursePageDto 페이징 조건 DTO
     * @return 페이징된 강의 목록
     */
    public RespPageDto<RespCourseListDto> findMyCourses(Long creatorId, ReqMyCoursePageDto reqMyCoursePageDto) {
        log.info("[CourseService] 나의 강의 목록 조회 - creatorId: {}, page: {}, size: {}", creatorId, reqMyCoursePageDto.getPage(), reqMyCoursePageDto.getSize());

        reqMyCoursePageDto.setCreatorId(creatorId);

        List<RespCourseListDto> content = courseMapper.selectCourseListByCreatorId(reqMyCoursePageDto);
        int totalCount = courseMapper.selectCourseListByCreatorIdCount(creatorId);

        return RespPageDto.of(content, reqMyCoursePageDto.getPage(), reqMyCoursePageDto.getSize(), totalCount);
    }

    /**
     * 강의별 수강생 목록 조회 (CREATOR 전용)
     * @param creatorId 크리에이터 ID
     * @param courseId 강의 ID
     * @param reqCourseEnrollmentPageDto 페이징 조건 DTO
     * @return 페이징된 수강생 목록
     * @throws BusinessException 강의 없음(400), 조회 권한 없음(403)
     */
    public RespPageDto<RespEnrollmentCreatorDto> findCourseEnrollments(Long creatorId, Long courseId, ReqCourseEnrollmentPageDto reqCourseEnrollmentPageDto) {
        log.info("[CourseService] 강의별 수강생 목록 조회 - creatorId: {}, courseId: {}", creatorId, courseId);

        // 강의 존재 여부 확인
        Course course = courseMapper.selectCourseById(courseId);
        if (course == null) {
            log.warn("[CourseService] 강의 없음 - courseId: {}", courseId);
            throw new BusinessException(HttpStatus.BAD_REQUEST, "존재하지 않는 강의입니다.");
        }

        // 크리에이터 권한 확인
        if (!course.getCreatorId().equals(creatorId)) {
            log.warn("[CourseService] 조회 권한 없음 - creatorId: {}, courseId: {}", creatorId, courseId);
            throw new BusinessException(HttpStatus.FORBIDDEN, "본인의 강의만 조회할 수 있습니다.");
        }

        reqCourseEnrollmentPageDto.setCourseId(courseId);

        List<RespEnrollmentCreatorDto> content = enrollmentMapper.selectEnrollmentListByCourseId(reqCourseEnrollmentPageDto);
        int totalCount = enrollmentMapper.selectEnrollmentListByCourseIdCount(courseId);

        return RespPageDto.of(content, reqCourseEnrollmentPageDto.getPage(), reqCourseEnrollmentPageDto.getSize(), totalCount);
    }
}