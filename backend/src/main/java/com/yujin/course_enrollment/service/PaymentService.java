package com.yujin.course_enrollment.service;

import com.yujin.course_enrollment.dto.req.ReqMyPaymentPageDto;
import com.yujin.course_enrollment.dto.req.ReqPaymentConfirmDto;
import com.yujin.course_enrollment.dto.req.ReqTossWebhookDto;
import com.yujin.course_enrollment.dto.resp.RespPageDto;
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

import java.util.List;

/**
 * 결제 서비스
 * 토스페이먼츠 결제 승인 및 수강 신청 상태 변경을 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private static final String TOSS_EVENT_PAYMENT_STATUS_CHANGED = "PAYMENT_STATUS_CHANGED";
    private static final String TOSS_STATUS_DONE = "DONE";
    private static final String TOSS_STATUS_CANCELED = "CANCELED";
    private static final String WEBHOOK_CANCEL_REASON = "Toss 웹훅 취소";

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

        // 강의 존재 여부 확인
        Course course = courseMapper.selectCourseById(enrollment.getCourseId());
        if (course == null) {
            log.warn("[PaymentService] 강의 없음 - courseId: {}", enrollment.getCourseId());
            throw new BusinessException(HttpStatus.BAD_REQUEST, "존재하지 않는 강의입니다.");
        }

        // 결제 금액 검증 (프론트 조작 방지)
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

    /**
     * 결제 환불
     * DONE 상태 결제를 찾아 토스 환불 API 호출 후 CANCELLED 업데이트
     * 무료 강의(결제 내역 없음)는 환불 없이 통과
     * @param enrollmentId 수강 신청 ID
     * @param cancelReason 취소 사유
     * @throws BusinessException 토스 환불 API 호출 실패(400)
     */
    @Transactional
    public void refund(Long enrollmentId, String cancelReason) {
        Payment payment = paymentMapper.selectPaymentByEnrollmentId(enrollmentId);

        if (payment == null) {
            log.info("[PaymentService] 결제 내역 없음 (무료 강의) - enrollmentId: {}", enrollmentId);
            return;
        }

        log.info("[PaymentService] 환불 요청 - enrollmentId: {}, paymentKey: {}", enrollmentId, payment.getPaymentKey());

        tossPaymentClient.cancel(payment.getPaymentKey(), cancelReason);
        paymentMapper.updatePaymentCancelled(Payment.ofCancelled(payment.getId(), cancelReason));

        log.info("[PaymentService] 환불 완료 - enrollmentId: {}, paymentKey: {}", enrollmentId, payment.getPaymentKey());
    }

    /**
     * Toss 웹훅 처리
     * PAYMENT_STATUS_CHANGED 이벤트 수신 시 상태별로 DB 동기화
     * - DONE: confirmPayment() DB 업데이트 실패 시 결제 완료 보정
     * - CANCELED: refund() 인라인 업데이트 실패 시 결제 취소 보정
     * @param reqTossWebhookDto 웹훅 페이로드
     */
    @Transactional
    public void handleTossWebhook(ReqTossWebhookDto reqTossWebhookDto) {
        // 결제 상태 변경 이벤트만 처리
        if (!TOSS_EVENT_PAYMENT_STATUS_CHANGED.equals(reqTossWebhookDto.getEventType())) {
            return;
        }

        ReqTossWebhookDto.TossPaymentData data = reqTossWebhookDto.getData();

        // data 누락 방어
        if (data == null || data.getOrderId() == null) {
            log.warn("[PaymentService] 웹훅 - data 누락 - eventType: {}", reqTossWebhookDto.getEventType());
            return;
        }

        log.info("[PaymentService] 웹훅 - status: {}, orderId: {}", data.getStatus(), data.getOrderId());

        // DONE: confirmPayment() DB 업데이트 실패 시 결제 완료 보정
        if (TOSS_STATUS_DONE.equals(data.getStatus())) {
            Payment payment = paymentMapper.selectPaymentByOrderId(data.getOrderId());
            if (payment == null) {
                log.warn("[PaymentService] 웹훅 - 결제 내역 없음 - orderId: {}", data.getOrderId());
                return;
            }

            if (PaymentStatus.DONE.equals(payment.getStatus())) {
                log.info("[PaymentService] 웹훅 - 이미 완료된 결제 - orderId: {}", data.getOrderId());
                return;
            }

            if (!PaymentStatus.PENDING.equals(payment.getStatus())) {
                log.warn("[PaymentService] 웹훅 - 처리 불가 상태 - orderId: {}, status: {}", data.getOrderId(), payment.getStatus());
                return;
            }

            paymentMapper.updatePaymentDone(Payment.ofDone(payment.getId(), data.getPaymentKey(), data.getMethod(), data.getApprovedAtAsLocalDateTime()));
            enrollmentMapper.updateEnrollmentStatus(Enrollment.ofConfirm(payment.getEnrollmentId()));

            log.info("[PaymentService] 웹훅 - 결제 완료 보정 - orderId: {}", data.getOrderId());

            return;
        }

        // CANCELED: refund() 인라인 업데이트 실패 시 결제 취소 보정
        if (!TOSS_STATUS_CANCELED.equals(data.getStatus())) {
            log.info("[PaymentService] 웹훅 - 처리 대상 아님 - status: {}", data.getStatus());
            return;
        }

        // 결제 내역 없음
        Payment payment = paymentMapper.selectPaymentByOrderId(data.getOrderId());
        if (payment == null) {
            log.warn("[PaymentService] 웹훅 - 결제 내역 없음 - orderId: {}", data.getOrderId());
            return;
        }

        // paymentKey 교차 검증
        if (!payment.getPaymentKey().equals(data.getPaymentKey())) {
            log.warn("[PaymentService] 웹훅 - paymentKey 불일치 - orderId: {}", data.getOrderId());
            return;
        }

        // 이미 CANCELLED 상태면 무시
        if (PaymentStatus.CANCELLED.equals(payment.getStatus())) {
            log.info("[PaymentService] 웹훅 - 이미 취소된 결제 - orderId: {}", data.getOrderId());
            return;
        }

        // 결제 CANCELLED 처리
        paymentMapper.updatePaymentCancelled(Payment.ofCancelled(payment.getId(), WEBHOOK_CANCEL_REASON));

        // enrollment 상태 동기화 (CONFIRMED → CANCELLED)
        Enrollment enrollment = enrollmentMapper.selectEnrollmentById(payment.getEnrollmentId());
        if (enrollment != null && EnrollmentStatus.CONFIRMED.equals(enrollment.getStatus())) {
            enrollmentMapper.updateEnrollmentStatus(Enrollment.ofCancel(payment.getEnrollmentId()));
            log.info("[PaymentService] 웹훅 - enrollment 취소 동기화 - enrollmentId: {}", payment.getEnrollmentId());
        }

        log.info("[PaymentService] 웹훅 - 결제 취소 처리 완료 - orderId: {}", data.getOrderId());
    }

    /**
     * 사용자 결제 내역 조회
     * @param userId 사용자 ID
     * @param reqMyPaymentPageDto 페이징 조건 DTO
     * @return 페이징된 결제 내역 목록 (DONE, CANCELLED)
     */
    public RespPageDto<RespPaymentDto> findMyPayments(Long userId, ReqMyPaymentPageDto reqMyPaymentPageDto) {
        log.info("[PaymentService] 결제 내역 조회 - userId: {}, page: {}, size: {}", userId, reqMyPaymentPageDto.getPage(), reqMyPaymentPageDto.getSize());

        reqMyPaymentPageDto.setUserId(userId);

        List<RespPaymentDto> content = paymentMapper.selectPaymentListByUserId(reqMyPaymentPageDto).stream()
                .map(RespPaymentDto::of)
                .toList();

        int totalCount = paymentMapper.selectPaymentListByUserIdCount(userId);

        return RespPageDto.of(content, reqMyPaymentPageDto.getPage(), reqMyPaymentPageDto.getSize(), totalCount);
    }
}
