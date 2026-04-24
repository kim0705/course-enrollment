package com.yujin.course_enrollment.mapper;

import com.yujin.course_enrollment.entity.Enrollment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 수강 신청 Mapper 인터페이스
 * EnrollmentMapper.xml과 매핑되어 수강 신청 관련 DB 접근을 담당
 */
@Mapper
public interface EnrollmentMapper {
    /* 수강 신청 등록 */
    void insertEnrollment(Enrollment enrollment);

    /* 수강 신청 단건 조회 */
    Enrollment selectEnrollmentById(Long id);

    /* 사용자-강의 수강 신청 조회 (중복 신청 검증용) */
    Enrollment selectEnrollmentByUserIdAndCourseId(@Param("userId") Long userId, @Param("courseId") Long courseId);

    /* 수강 신청 상태 변경 */
    void updateEnrollmentStatus(Enrollment enrollment);
}
