package com.yujin.course_enrollment.service;

import com.yujin.course_enrollment.dto.resp.RespAdminDashboardDto;
import com.yujin.course_enrollment.mapper.CourseMapper;
import com.yujin.course_enrollment.mapper.EnrollmentMapper;
import com.yujin.course_enrollment.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
