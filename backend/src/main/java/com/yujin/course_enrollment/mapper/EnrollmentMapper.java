package com.yujin.course_enrollment.mapper;

import com.yujin.course_enrollment.dto.req.ReqCourseEnrollmentPageDto;
import com.yujin.course_enrollment.dto.req.ReqEnrollmentPageDto;
import com.yujin.course_enrollment.dto.resp.RespEnrollmentCreatorDto;
import com.yujin.course_enrollment.dto.resp.RespEnrollmentStudentDto;
import com.yujin.course_enrollment.entity.Enrollment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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

    /* 재신청 상태 변경 (CANCELLED 상태일 때만, 반환값: 업데이트된 행 수) */
    int updateEnrollmentStatusReEnroll(Enrollment enrollment);

    /* 나의 수강 신청 목록 조회 */
    List<RespEnrollmentStudentDto> selectEnrollmentListByUserId(ReqEnrollmentPageDto reqEnrollmentPageDto);

    /* 나의 수강 신청 목록 전체 수 조회 (페이징용) */
    int selectEnrollmentListByUserIdCount(Long userId);

    /* 강의별 수강생 목록 조회 (CREATOR 전용) */
    List<RespEnrollmentCreatorDto> selectEnrollmentListByCourseId(ReqCourseEnrollmentPageDto reqCourseEnrollmentPageDto);

    /* 강의별 수강생 수 조회 (페이징용) */
    int selectEnrollmentListByCourseIdCount(Long courseId);

    /* 대기열 첫 번째 조회 (자동 승격용) */
    Enrollment selectNextWaitlist(Long courseId);

    /* 대기열 승격 (WAITLIST 상태일 때만 PENDING으로 변경) */
    int updateEnrollmentStatusPromote(Long id);

    /* 수강 신청 수 조회 (PENDING + CONFIRMED) */
    int selectActiveEnrollmentCount();

    /* 강의 마감 시 대기열 전체 취소 */
    int updateWaitlistCancelledByCourseId(Long courseId);

    /* 강제 폐강 시 CONFIRMED 수강 신청 ID 목록 조회 */
    List<Long> selectConfirmedEnrollmentIdsByCourseId(Long courseId);

    /* 강제 폐강 시 미결제 수강 신청 취소 (WAITLIST, PENDING → CANCELLED) */
    int updatePendingWaitlistCancelledByCourseId(Long courseId);
}
