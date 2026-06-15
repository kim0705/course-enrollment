package com.yujin.course_enrollment.service;

import com.yujin.course_enrollment.dto.req.ReqAdminCoursePageDto;
import com.yujin.course_enrollment.dto.req.ReqAdminPaymentPageDto;
import com.yujin.course_enrollment.dto.resp.RespAdminDashboardDto;
import com.yujin.course_enrollment.dto.resp.RespAdminPaymentDto;
import com.yujin.course_enrollment.dto.resp.RespCourseListDto;
import com.yujin.course_enrollment.entity.Payment;
import com.yujin.course_enrollment.global.PaymentStatus;
import com.yujin.course_enrollment.dto.resp.RespPageDto;
import com.yujin.course_enrollment.entity.Course;
import com.yujin.course_enrollment.entity.Enrollment;
import com.yujin.course_enrollment.entity.User;
import com.yujin.course_enrollment.global.CourseStatus;
import com.yujin.course_enrollment.global.exception.BusinessException;
import com.yujin.course_enrollment.mapper.CourseMapper;
import com.yujin.course_enrollment.mapper.EnrollmentMapper;
import com.yujin.course_enrollment.mapper.PaymentMapper;
import com.yujin.course_enrollment.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.transaction.support.TransactionTemplate;

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

    private static final int MAX_REFUND_RETRIES = 2;

    private final UserMapper userMapper;
    private final CourseMapper courseMapper;
    private final EnrollmentMapper enrollmentMapper;
    private final PaymentMapper paymentMapper;
    private final PaymentService paymentService;
    private final TransactionTemplate transactionTemplate;

    /**
     * 대시보드 통계 조회
     * @return 전체 사용자 수, 강의 수, 확정 수강 신청 수, 결제 통계
     */
    public RespAdminDashboardDto getDashboardStats() {
        log.info("[AdminService] 대시보드 통계 조회");

        int totalUsers = userMapper.selectUserCount();
        int studentCount = userMapper.selectUserCountByRole("STUDENT");
        int creatorCount = userMapper.selectUserCountByRole("CREATOR");
        int totalCourses = courseMapper.selectAdminCourseListCount();
        int draftCount = courseMapper.selectCourseCountByStatus("DRAFT");
        int openCount = courseMapper.selectCourseCountByStatus("OPEN");
        int closedCount = courseMapper.selectCourseCountByStatus("CLOSED");
        int forceClosedCount = courseMapper.selectCourseCountByStatus("FORCE_CLOSED");
        int totalEnrollments = enrollmentMapper.selectActiveEnrollmentCount();
        Long totalRevenue = paymentMapper.selectTotalRevenue();
        Long totalRefund = paymentMapper.selectTotalRefund();
        Long monthlyRevenue = paymentMapper.selectMonthlyRevenue();

        return RespAdminDashboardDto.of(totalUsers, studentCount, creatorCount, totalCourses, draftCount, openCount, closedCount, forceClosedCount, totalEnrollments, totalRevenue, totalRefund, monthlyRevenue);
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
     * 관리자 전체 결제 내역 조회
     * @param reqAdminPaymentPageDto 페이징 조건 (status 필터 선택)
     * @return 페이징된 결제 내역 (DONE, CANCELLED)
     */
    public RespPageDto<RespAdminPaymentDto> findAdminPayments(ReqAdminPaymentPageDto reqAdminPaymentPageDto) {
        log.info("[AdminService] 전체 결제 내역 조회 - page: {}, size: {}, status: {}", reqAdminPaymentPageDto.getPage(), reqAdminPaymentPageDto.getSize(), reqAdminPaymentPageDto.getStatus());

        List<RespAdminPaymentDto> content = paymentMapper.selectAdminPaymentList(reqAdminPaymentPageDto);
        int totalCount = paymentMapper.selectAdminPaymentListCount(reqAdminPaymentPageDto);

        return RespPageDto.of(content, reqAdminPaymentPageDto.getPage(), reqAdminPaymentPageDto.getSize(), totalCount);
    }

    /**
     * 강의 강제 폐강
     * @param courseId 강의 ID
     * @throws BusinessException 강의 없음(404), 이미 폐강(400)
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void forceCloseCourse(Long courseId) {
        log.info("[AdminService] 강의 강제 폐강 - courseId: {}", courseId);

        Course course = courseMapper.selectCourseById(courseId);
        if (course == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "존재하지 않는 강의입니다.");
        }

        if (CourseStatus.CLOSED.equals(course.getStatus()) || CourseStatus.FORCE_CLOSED.equals(course.getStatus())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "이미 폐강된 강의입니다.");
        }

        // 강의 폐강 + WAITLIST/PENDING 즉시 취소 (트랜잭션)
        // CONFIRMED ID는 트랜잭션 안에서 조회해 일관된 스냅샷 보장
        List<Long> confirmedIds = transactionTemplate.execute(status -> {
            List<Long> ids = enrollmentMapper.selectConfirmedEnrollmentIdsByCourseId(courseId);
            enrollmentMapper.updatePendingWaitlistCancelledByCourseId(courseId);
            courseMapper.updateCourseEnrolledCountReset(courseId);
            courseMapper.updateCourseStatus(Course.ofForceClosed(courseId));
            return ids;
        });

        if (confirmedIds == null || confirmedIds.isEmpty()) {
            log.info("[AdminService] 강의 강제 폐강 완료 - courseId: {}, 환불 대상 없음", courseId);
            return;
        }

        // CONFIRMED 건별 환불 후 취소 — 실패 시 재시도 후 REFUND_FAILED 마킹
        int successCount = 0;
        for (Long enrollmentId : confirmedIds) {
            boolean refunded = false;

            for (int attempt = 1; attempt <= MAX_REFUND_RETRIES; attempt++) {
                try {
                    paymentService.refund(enrollmentId, "강의 폐강");
                    refunded = true;
                    break;
                } catch (Exception e) {
                    log.warn("[AdminService] 환불 시도 {}/{} 실패 - enrollmentId: {}", attempt, MAX_REFUND_RETRIES, enrollmentId, e);
                }
            }

            // 재시도 성공
            if (refunded) {
                transactionTemplate.executeWithoutResult(s ->
                        enrollmentMapper.updateEnrollmentStatus(Enrollment.ofForceClose(enrollmentId)));
                successCount++;
            // 재시도 최종 실패 → REFUND_FAILED 마킹
            } else {
                log.error("[AdminService] 환불 최종 실패 - REFUND_FAILED 마킹 - enrollmentId: {}", enrollmentId);
                transactionTemplate.executeWithoutResult(s ->
                        paymentMapper.updatePaymentRefundFailed(enrollmentId));
            }
        }

        log.info("[AdminService] 강의 강제 폐강 완료 - courseId: {}, 환불 성공: {}/{}", courseId, successCount, confirmedIds.size());
    }

    /**
     * 환불 실패 건 수동 재시도
     * REFUND_FAILED 상태 수강 신청에 대해 Toss 환불 API 재호출
     * @param enrollmentId 수강 신청 ID
     * @throws BusinessException 수강 신청 없음(404), REFUND_FAILED 상태 아님(400)
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void retryRefund(Long enrollmentId) {
        log.info("[AdminService] 환불 재시도 - enrollmentId: {}", enrollmentId);

        Enrollment enrollment = enrollmentMapper.selectEnrollmentById(enrollmentId);
        if (enrollment == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "존재하지 않는 수강 신청입니다.");
        }

        Payment payment = paymentMapper.selectPaymentByEnrollmentId(enrollmentId);
        if (payment == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "결제 내역이 없습니다.");
        }

        if (!PaymentStatus.REFUND_FAILED.equals(payment.getStatus())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "환불 실패 상태의 결제만 재시도할 수 있습니다.");
        }

        paymentService.refund(enrollmentId, "강의 폐강");
        transactionTemplate.executeWithoutResult(s ->
                enrollmentMapper.updateEnrollmentStatus(Enrollment.ofForceClose(enrollmentId)));

        log.info("[AdminService] 환불 재시도 성공 - enrollmentId: {}", enrollmentId);
    }
}
