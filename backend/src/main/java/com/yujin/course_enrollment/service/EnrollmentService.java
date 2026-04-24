package com.yujin.course_enrollment.service;

import com.yujin.course_enrollment.dto.req.ReqEnrollmentCreateDto;
import com.yujin.course_enrollment.dto.resp.RespEnrollmentDto;
import com.yujin.course_enrollment.entity.Course;
import com.yujin.course_enrollment.entity.Enrollment;
import com.yujin.course_enrollment.entity.User;
import com.yujin.course_enrollment.global.exception.BusinessException;
import com.yujin.course_enrollment.mapper.CourseMapper;
import com.yujin.course_enrollment.mapper.EnrollmentMapper;
import com.yujin.course_enrollment.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 수강 신청 서비스
 * 수강 신청 등록 등 수강 신청 관련 비즈니스 로직을 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentService {

    private final EnrollmentMapper enrollmentMapper;
    private final CourseMapper courseMapper;
    private final UserMapper userMapper;

    /**
     * 수강 신청
     * @param userId 신청하는 사용자 ID
     * @param reqEnrollmentCreateDto 수강 신청 요청 DTO
     * @throws BusinessException 사용자 없음(400), 강의 없음(400), 본인 강의 신청(403), 모집 중 아님(400), 중복 신청(400), 정원 초과(400)
     */
    @Transactional
    public RespEnrollmentDto registerEnrollment(Long userId, ReqEnrollmentCreateDto reqEnrollmentCreateDto) {
        log.info("[EnrollmentService] 수강 신청 - userId: {}, courseId: {}", userId, reqEnrollmentCreateDto.getCourseId());

        // 사용자 존재 여부 확인
        User user = userMapper.selectUserById(userId);
        if (user == null) {
            log.warn("[EnrollmentService] 존재하지 않는 사용자 - userId: {}", userId);
            throw new BusinessException(HttpStatus.BAD_REQUEST, "존재하지 않는 사용자입니다.");
        }

        Long courseId = reqEnrollmentCreateDto.getCourseId();

        // 강의 존재 여부 확인
        Course course = courseMapper.selectCourseById(courseId);
        if (course == null) {
            log.warn("[EnrollmentService] 강의 없음 - courseId: {}", courseId);
            throw new BusinessException(HttpStatus.BAD_REQUEST, "존재하지 않는 강의입니다.");
        }

        // 본인 강의 신청 불가
        if (course.getCreatorId().equals(userId)) {
            log.warn("[EnrollmentService] 본인 강의 신청 시도 - userId: {}, courseId: {}", userId, courseId);
            throw new BusinessException(HttpStatus.FORBIDDEN, "본인이 개설한 강의는 신청할 수 없습니다.");
        }

        // 강의 모집 중 확인
        if (!"OPEN".equals(course.getStatus())) {
            log.warn("[EnrollmentService] 모집 중이 아님 - courseId: {}, status: {}", courseId, course.getStatus());
            throw new BusinessException(HttpStatus.BAD_REQUEST, "모집 중인 강의만 신청할 수 있습니다.");
        }

        // 중복 신청 확인
        Enrollment existing = enrollmentMapper.selectEnrollmentByUserIdAndCourseId(userId, courseId);
        if (existing != null) {
            log.warn("[EnrollmentService] 중복 신청 - userId: {}, courseId: {}", userId, courseId);
            throw new BusinessException(HttpStatus.BAD_REQUEST, "이미 신청한 강의입니다.");
        }

        // 정원 초과 확인
        if (course.getEnrolledCount() >= course.getCapacity()) {
            log.warn("[EnrollmentService] 정원 초과 - courseId: {}, enrolledCount: {}, capacity: {}", courseId, course.getEnrolledCount(), course.getCapacity());
            throw new BusinessException(HttpStatus.BAD_REQUEST, "수강 정원이 초과되었습니다.");
        }

        Enrollment enrollment = reqEnrollmentCreateDto.toEntity(userId);
        enrollmentMapper.insertEnrollment(enrollment);

        // 수강 인원 증가 (0이면 동시 신청으로 정원 초과)
        int updated = courseMapper.incrementEnrolledCount(courseId);
        if (updated == 0) {
            log.warn("[EnrollmentService] 정원 초과 (동시 신청) - courseId: {}", courseId);
            throw new BusinessException(HttpStatus.BAD_REQUEST, "수강 정원이 초과되었습니다.");
        }

        log.info("[EnrollmentService] 수강 신청 완료 - enrollmentId: {}", enrollment.getId());

        Enrollment saved = enrollmentMapper.selectEnrollmentById(enrollment.getId());

        return RespEnrollmentDto.of(saved, course.getTitle());
    }
}
