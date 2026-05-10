package com.yujin.course_enrollment.service;

import com.yujin.course_enrollment.dto.req.ReqPaymentConfirmDto;
import com.yujin.course_enrollment.dto.resp.RespPaymentDto;
import com.yujin.course_enrollment.entity.Course;
import com.yujin.course_enrollment.entity.Enrollment;
import com.yujin.course_enrollment.entity.Payment;
import com.yujin.course_enrollment.global.EnrollmentStatus;
import com.yujin.course_enrollment.global.PaymentStatus;
import com.yujin.course_enrollment.global.exception.BusinessException;
import com.yujin.course_enrollment.mapper.CourseMapper;
import com.yujin.course_enrollment.mapper.EnrollmentMapper;
import com.yujin.course_enrollment.mapper.PaymentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 결제 서비스
 * 토스페이먼츠 결제 승인 및 수강 신청 상태 변경을 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentMapper paymentMapper;
    private final EnrollmentMapper enrollmentMapper;
    private final CourseMapper courseMapper;
    private final TossPaymentClient tossPaymentClient;

    /**
     * 토스페이먼츠 결제 승인
     * 결제 전 금액을 DB 강의 가격과 교차 검증하고, PENDING 선저장 후 승인
     * 토스 API 실패 시 FAILED로 업데이트 (noRollbackFor로 커밋 보장)
     * @param userId 사용자 ID
     * @param reqPaymentConfirmDto 결제 승인 요청 DTO (enrollmentId, paymentKey, orderId, orderName, amount)
     * @return 저장된 결제 응답 DTO
     * @throws BusinessException 수강 신청 없음(400), 본인 신청 아님(403), PENDING 아님(400), 금액 불일치(400), 중복 결제(400), 토스 API 실패(400)
     */
    @Transactional(noRollbackFor = BusinessException.class)
    public RespPaymentDto confirmPayment(Long userId, ReqPaymentConfirmDto reqPaymentConfirmDto) {
        log.info("[PaymentService] 결제 승인 - userId: {}, enrollmentId: {}, orderId: {}", userId, reqPaymentConfirmDto.getEnrollmentId(), reqPaymentConfirmDto.getOrderId());

        // 수강 신청 존재 여부 확인
        Enrollment enrollment = enrollmentMapper.selectEnrollmentById(reqPaymentConfirmDto.getEnrollmentId());
        if (enrollment == null) {
            log.warn("[PaymentService] 수강 신청 없음 - enrollmentId: {}", reqPaymentConfirmDto.getEnrollmentId());
            throw new BusinessException(HttpStatus.BAD_REQUEST, "존재하지 않는 수강 신청입니다.");
        }

        // 본인 신청 확인
        if (!enrollment.getUserId().equals(userId)) {
            log.warn("[PaymentService] 본인 신청 아님 - userId: {}, enrollmentId: {}", userId, reqPaymentConfirmDto.getEnrollmentId());
            throw new BusinessException(HttpStatus.FORBIDDEN, "본인의 수강 신청만 결제할 수 있습니다.");
        }

        // PENDING 상태 확인
        if (!EnrollmentStatus.PENDING.equals(enrollment.getStatus())) {
            log.warn("[PaymentService] PENDING 상태 아님 - enrollmentId: {}, status: {}", reqPaymentConfirmDto.getEnrollmentId(), enrollment.getStatus());
            throw new BusinessException(HttpStatus.BAD_REQUEST, "PENDING 상태의 수강 신청만 결제할 수 있습니다.");
        }

        // 결제 금액 검증 (프론트 조작 방지)
        Course course = courseMapper.selectCourseById(enrollment.getCourseId());
        if (course == null) {
            log.warn("[PaymentService] 강의 없음 - courseId: {}", enrollment.getCourseId());
            throw new BusinessException(HttpStatus.BAD_REQUEST, "존재하지 않는 강의입니다.");
        }

        int coursePrice = course.getPrice();
        if (coursePrice != reqPaymentConfirmDto.getAmount()) {
            log.warn("[PaymentService] 금액 불일치 - enrollmentId: {}, coursePrice: {}, reqAmount: {}", reqPaymentConfirmDto.getEnrollmentId(), coursePrice, reqPaymentConfirmDto.getAmount());
            throw new BusinessException(HttpStatus.BAD_REQUEST, "결제 금액이 강의 가격과 일치하지 않습니다.");
        }

        // PENDING 선저장 (INSERT IGNORE: 중복 orderId는 무시하고 기존 레코드 유지)
        Payment pending = Payment.ofPending(reqPaymentConfirmDto.getEnrollmentId(), reqPaymentConfirmDto.getOrderId(), reqPaymentConfirmDto.getOrderName(), reqPaymentConfirmDto.getAmount());
        paymentMapper.insertPayment(pending);

        Payment inserted = paymentMapper.selectPaymentByOrderId(reqPaymentConfirmDto.getOrderId());

        // 이미 완료된 결제 (페이지 새로고침 등 재요청) → 기존 결과 반환
        if (PaymentStatus.DONE.equals(inserted.getStatus())) {
            log.info("[PaymentService] 이미 완료된 결제 재요청 - orderId: {}", reqPaymentConfirmDto.getOrderId());
            return RespPaymentDto.of(inserted);
        }

        // PENDING 이외 상태 (FAILED 등) → 재시도 불가
        if (!PaymentStatus.PENDING.equals(inserted.getStatus())) {
            log.warn("[PaymentService] 처리 불가 상태 - orderId: {}, status: {}", reqPaymentConfirmDto.getOrderId(), inserted.getStatus());
            throw new BusinessException(HttpStatus.BAD_REQUEST, "처리할 수 없는 결제 상태입니다.");
        }

        // 토스 API 결제 승인
        try {
            TossPaymentClient.TossConfirmResponse tossResponse = tossPaymentClient.confirm(reqPaymentConfirmDto.getPaymentKey(), reqPaymentConfirmDto.getOrderId(), reqPaymentConfirmDto.getAmount());

            // 결제 완료: DONE 업데이트 + enrollment CONFIRMED
            paymentMapper.updatePaymentDone(Payment.ofDone(inserted.getId(), tossResponse.getPaymentKey(), tossResponse.getMethod(), tossResponse.getApprovedAtAsLocalDateTime()));
            enrollmentMapper.updateEnrollmentStatus(Enrollment.ofConfirm(reqPaymentConfirmDto.getEnrollmentId()));

        } catch (BusinessException e) {
            // 토스 API 실패: FAILED 업데이트 후 예외 전파 (noRollbackFor로 커밋됨)
            log.warn("[PaymentService] 결제 승인 실패 - enrollmentId: {}, orderId: {}", reqPaymentConfirmDto.getEnrollmentId(), reqPaymentConfirmDto.getOrderId());
            paymentMapper.updatePaymentFailed(inserted.getId());
            throw e;
        }

        log.info("[PaymentService] 결제 승인 완료 - enrollmentId: {}, orderId: {}", reqPaymentConfirmDto.getEnrollmentId(), reqPaymentConfirmDto.getOrderId());

        return RespPaymentDto.of(paymentMapper.selectPaymentByOrderId(reqPaymentConfirmDto.getOrderId()));
    }
}
