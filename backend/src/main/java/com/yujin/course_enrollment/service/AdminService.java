package com.yujin.course_enrollment.service;

import com.yujin.course_enrollment.dto.req.ReqAdminCoursePageDto;
import com.yujin.course_enrollment.dto.resp.RespAdminDashboardDto;
import com.yujin.course_enrollment.dto.resp.RespCourseListDto;
import com.yujin.course_enrollment.dto.resp.RespPageDto;
import com.yujin.course_enrollment.entity.Course;
import com.yujin.course_enrollment.entity.User;
import com.yujin.course_enrollment.global.CourseStatus;
import com.yujin.course_enrollment.global.exception.BusinessException;
import com.yujin.course_enrollment.mapper.CourseMapper;
import com.yujin.course_enrollment.mapper.EnrollmentMapper;
import com.yujin.course_enrollment.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 관리자 서비스
 * 관리자 전용 비즈니스 로직을 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final UserMapper userMapper;
    private final CourseMapper courseMapper;
    private final EnrollmentMapper enrollmentMapper;

    /**
     * 대시보드 통계 조회
     * @return 전체 사용자 수, 강의 수, 확정 수강 신청 수
     */
    public RespAdminDashboardDto getDashboardStats() {
        log.info("[AdminService] 대시보드 통계 조회");

        return RespAdminDashboardDto.of(userMapper.selectUserCount(), courseMapper.selectAdminCourseListCount(), enrollmentMapper.selectConfirmedEnrollmentCount());
    }

    /**
     * 전체 사용자 목록 조회
     * @return 전체 사용자 목록
     */
    public List<User> findAllUsers() {
        log.info("[AdminService] 전체 사용자 목록 조회");

        return userMapper.selectUserList();
    }

    /**
     * 사용자 역할 변경
     * @param userId 대상 사용자 ID
     * @param role 변경할 역할
     * @throws BusinessException 사용자 없음(404), ADMIN 역할 변경 시도(400)
     */
    @Transactional
    public void updateUserRole(Long userId, String role) {
        log.info("[AdminService] 사용자 역할 변경 - userId: {}, role: {}", userId, role);

        User user = userMapper.selectUserById(userId);
        if (user == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다.");
        }

        if ("ADMIN".equals(role)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "관리자 역할로 변경할 수 없습니다.");
        }

        userMapper.updateUserRole(userId, role);
    }

    /**
     * 전체 강의 목록 조회 (모든 상태 포함)
     * @param reqAdminCoursePageDto 페이징 조건
     * @return 페이징된 강의 목록
     */
    public RespPageDto<RespCourseListDto> findAllCourses(ReqAdminCoursePageDto reqAdminCoursePageDto) {
        log.info("[AdminService] 전체 강의 목록 조회 - page: {}, size: {}", reqAdminCoursePageDto.getPage(), reqAdminCoursePageDto.getSize());

        List<RespCourseListDto> content = courseMapper.selectAdminCourseList(reqAdminCoursePageDto);
        int totalCount = courseMapper.selectAdminCourseListCount();

        return RespPageDto.of(content, reqAdminCoursePageDto.getPage(), reqAdminCoursePageDto.getSize(), totalCount);
    }

    /**
     * 강의 강제 폐강
     * @param courseId 강의 ID
     * @throws BusinessException 강의 없음(404), 이미 폐강(400)
     */
    @Transactional
    public void forceCloseCourse(Long courseId) {
        log.info("[AdminService] 강의 강제 폐강 - courseId: {}", courseId);

        Course course = courseMapper.selectCourseById(courseId);
        if (course == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "존재하지 않는 강의입니다.");
        }

        if (CourseStatus.CLOSED.equals(course.getStatus())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "이미 폐강된 강의입니다.");
        }

        courseMapper.updateCourseStatus(Course.builder().id(courseId).status(CourseStatus.CLOSED).build());
    }
}
