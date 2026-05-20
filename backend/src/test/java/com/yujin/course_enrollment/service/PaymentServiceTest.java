package com.yujin.course_enrollment.service;

import com.yujin.course_enrollment.dto.req.ReqMyPaymentPageDto;
import com.yujin.course_enrollment.dto.req.ReqPaymentConfirmDto;
import com.yujin.course_enrollment.dto.req.ReqTossWebhookDto;
import com.yujin.course_enrollment.dto.resp.RespPageDto;
import com.yujin.course_enrollment.dto.resp.RespPaymentDto;
import com.yujin.course_enrollment.entity.Course;
import com.yujin.course_enrollment.entity.Enrollment;
import com.yujin.course_enrollment.entity.Payment;
import com.yujin.course_enrollment.global.exception.BusinessException;
import com.yujin.course_enrollment.mapper.CourseMapper;
import com.yujin.course_enrollment.mapper.EnrollmentMapper;
import com.yujin.course_enrollment.mapper.PaymentMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

/**
 * 결제 서비스 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private EnrollmentMapper enrollmentMapper;

    @Mock
    private CourseMapper courseMapper;

    @Mock
    private TossPaymentClient tossPaymentClient;

    @Test
    @DisplayName("결제 승인 성공")
    void confirmPayment_success() {
        // given
        Long userId = 4L;
        ReqPaymentConfirmDto req = new ReqPaymentConfirmDto(1L, "tgen_abc123", "order_001", "Spring Boot 강의", 50000);
        Enrollment pending = Enrollment.builder()
                .id(1L)
                .userId(userId)
                .courseId(1L)
                .status("PENDING").build();
        Course course = Course.builder()
                .id(1L)
                .price(50000)
                .build();
        TossPaymentClient.TossConfirmResponse tossResponse = new TossPaymentClient.TossConfirmResponse(
                "tgen_abc123", "order_001", "Spring Boot 강의", 50000, "카드", "DONE", "2024-01-01T10:00:00+09:00"
        );
        Payment inserted = Payment.builder()
                .id(1L)
                .enrollmentId(1L)
                .orderId("order_001")
                .status("PENDING")
                .build();
        Payment done = Payment.builder()
                .id(1L)
                .enrollmentId(1L)
                .paymentKey("tgen_abc123")
                .orderId("order_001")
                .orderName("Spring Boot 강의")
                .amount(50000).method("카드")
                .status("DONE")
                .paidAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        given(enrollmentMapper.selectEnrollmentById(1L)).willReturn(pending);
        given(courseMapper.selectCourseById(1L)).willReturn(course);
        given(paymentMapper.selectPaymentByOrderId("order_001")).willReturn(inserted).willReturn(done);
        willDoNothing().given(paymentMapper).insertPayment(any());
        given(tossPaymentClient.confirm("tgen_abc123", "order_001", 50000)).willReturn(tossResponse);
        willDoNothing().given(paymentMapper).updatePaymentDone(any());
        willDoNothing().given(enrollmentMapper).updateEnrollmentStatus(any());

        // when
        RespPaymentDto result = paymentService.confirmPayment(userId, req);

        // then
        assertThat(result.getOrderId()).isEqualTo("order_001");
        assertThat(result.getStatus()).isEqualTo("DONE");
        assertThat(result.getAmount()).isEqualTo(50000);
        then(paymentMapper).should().insertPayment(any());
        then(paymentMapper).should().updatePaymentDone(any());
        then(enrollmentMapper).should().updateEnrollmentStatus(any());
    }

    @Test
    @DisplayName("존재하지 않는 수강 신청 - 결제 승인 실패")
    void confirmPayment_fail_enrollmentNotFound() {
        // given
        Long userId = 4L;
        ReqPaymentConfirmDto req = new ReqPaymentConfirmDto(999L, "tgen_abc123", "order_001", "강의명", 50000);
        given(enrollmentMapper.selectEnrollmentById(999L)).willReturn(null);

        // when & then
        assertThatThrownBy(() -> paymentService.confirmPayment(userId, req))
                .isInstanceOf(BusinessException.class)
                .hasMessage("존재하지 않는 수강 신청입니다.");
    }

    @Test
    @DisplayName("본인 신청 아님 - 결제 승인 실패")
    void confirmPayment_fail_notOwner() {
        // given
        Long userId = 99L;
        ReqPaymentConfirmDto req = new ReqPaymentConfirmDto(1L, "tgen_abc123", "order_001", "강의명", 50000);
        Enrollment pending = Enrollment.builder()
                .id(1L)
                .userId(4L)
                .courseId(1L)
                .status("PENDING")
                .build();
        given(enrollmentMapper.selectEnrollmentById(1L)).willReturn(pending);

        // when & then
        assertThatThrownBy(() -> paymentService.confirmPayment(userId, req))
                .isInstanceOf(BusinessException.class)
                .hasMessage("본인의 수강 신청만 결제할 수 있습니다.");
    }

    @Test
    @DisplayName("PENDING 상태 아님 - 결제 승인 실패")
    void confirmPayment_fail_notPending() {
        // given
        Long userId = 4L;
        ReqPaymentConfirmDto req = new ReqPaymentConfirmDto(1L, "tgen_abc123", "order_001", "강의명", 50000);
        Enrollment confirmed = Enrollment.builder()
                .id(1L)
                .userId(userId)
                .courseId(1L)
                .status("CONFIRMED")
                .build();
        given(enrollmentMapper.selectEnrollmentById(1L)).willReturn(confirmed);

        // when & then
        assertThatThrownBy(() -> paymentService.confirmPayment(userId, req))
                .isInstanceOf(BusinessException.class)
                .hasMessage("PENDING 상태의 수강 신청만 결제할 수 있습니다.");
    }

    @Test
    @DisplayName("금액 불일치 - 결제 승인 실패")
    void confirmPayment_fail_amountMismatch() {
        // given
        Long userId = 4L;
        ReqPaymentConfirmDto req = new ReqPaymentConfirmDto(1L, "tgen_abc123", "order_001", "강의명", 30000);
        Enrollment pending = Enrollment.builder()
                .id(1L)
                .userId(userId)
                .courseId(1L)
                .status("PENDING")
                .build();
        Course course = Course.builder()
                .id(1L)
                .price(50000)
                .build();

        given(enrollmentMapper.selectEnrollmentById(1L)).willReturn(pending);
        given(courseMapper.selectCourseById(1L)).willReturn(course);

        // when & then
        assertThatThrownBy(() -> paymentService.confirmPayment(userId, req))
                .isInstanceOf(BusinessException.class)
                .hasMessage("결제 금액이 강의 가격과 일치하지 않습니다.");
    }

    @Test
    @DisplayName("중복 결제 - 결제 승인 실패")
    void confirmPayment_fail_duplicate() {
        // given
        Long userId = 4L;
        ReqPaymentConfirmDto req = new ReqPaymentConfirmDto(1L, "tgen_abc123", "order_001", "강의명", 50000);
        Enrollment pending = Enrollment.builder()
                .id(1L)
                .userId(userId).courseId(1L)
                .status("PENDING")
                .build();
        Course course = Course.builder()
                .id(1L)
                .price(50000)
                .build();
        Payment existing = Payment.builder()
                .id(1L)
                .orderId("order_001")
                .status("DONE")
                .build();
        given(enrollmentMapper.selectEnrollmentById(1L)).willReturn(pending);
        given(courseMapper.selectCourseById(1L)).willReturn(course);
        given(paymentMapper.selectPaymentByOrderId("order_001")).willReturn(existing);
        willDoNothing().given(paymentMapper).insertPayment(any());

        // when — 이미 DONE인 경우 멱등 처리: 기존 결과 반환
        RespPaymentDto result = paymentService.confirmPayment(userId, req);

        // then
        assertThat(result.getOrderId()).isEqualTo("order_001");
        assertThat(result.getStatus()).isEqualTo("DONE");
        then(tossPaymentClient).should(never()).confirm(any(), any(), anyInt());
    }

    @Test
    @DisplayName("토스 API 실패 - FAILED 업데이트 후 예외 전파")
    void confirmPayment_fail_tossApiError() {
        // given
        Long userId = 4L;
        ReqPaymentConfirmDto req = new ReqPaymentConfirmDto(1L, "tgen_invalid", "order_001", "강의명", 50000);
        Enrollment pending = Enrollment.builder()
                .id(1L)
                .userId(userId)
                .courseId(1L)
                .status("PENDING")
                .build();
        Course course = Course.builder()
                .id(1L)
                .price(50000)
                .build();
        Payment inserted = Payment.builder()
                .id(1L)
                .orderId("order_001")
                .status("PENDING")
                .build();

        given(enrollmentMapper.selectEnrollmentById(1L)).willReturn(pending);
        given(courseMapper.selectCourseById(1L)).willReturn(course);
        given(paymentMapper.selectPaymentByOrderId("order_001")).willReturn(inserted);
        willDoNothing().given(paymentMapper).insertPayment(any());
        given(tossPaymentClient.confirm("tgen_invalid", "order_001", 50000)).willThrow(new BusinessException(org.springframework.http.HttpStatus.BAD_REQUEST, "결제 승인에 실패했습니다."));

        // when & then
        assertThatThrownBy(() -> paymentService.confirmPayment(userId, req))
                .isInstanceOf(BusinessException.class)
                .hasMessage("결제 승인에 실패했습니다.");

        then(paymentMapper).should().insertPayment(any());
        then(paymentMapper).should().updatePaymentFailed(1L);
        then(paymentMapper).should(never()).updatePaymentDone(any());
        then(enrollmentMapper).should(never()).updateEnrollmentStatus(any());
    }

    @Test
    @DisplayName("환불 성공 - 토스 취소 API 호출 후 CANCELLED 업데이트")
    void refund_success() {
        // given
        Long enrollmentId = 1L;
        String cancelReason = "단순 변심";
        Payment done = Payment.builder()
                .id(1L)
                .enrollmentId(enrollmentId)
                .paymentKey("tgen_abc123")
                .orderId("order_001")
                .status("DONE")
                .build();

        given(paymentMapper.selectPaymentByEnrollmentId(enrollmentId)).willReturn(done);
        willDoNothing().given(tossPaymentClient).cancel("tgen_abc123", cancelReason);
        willDoNothing().given(paymentMapper).updatePaymentCancelled(any());

        // when
        paymentService.refund(enrollmentId, cancelReason);

        // then
        then(tossPaymentClient).should().cancel("tgen_abc123", cancelReason);
        then(paymentMapper).should().updatePaymentCancelled(any());
    }

    @Test
    @DisplayName("환불 스킵 - 무료 강의(결제 내역 없음)")
    void refund_skip_freeCourse() {
        // given
        Long enrollmentId = 2L;
        given(paymentMapper.selectPaymentByEnrollmentId(enrollmentId)).willReturn(null);

        // when
        paymentService.refund(enrollmentId, "단순 변심");

        // then
        then(tossPaymentClient).should(never()).cancel(any(), any());
        then(paymentMapper).should(never()).updatePaymentCancelled(any());
    }

    @Test
    @DisplayName("웹훅 - 정상 취소 처리 - payment CANCELLED + enrollment CANCELLED 동기화")
    void handleTossWebhook_success() {
        // given
        ReqTossWebhookDto reqTossWebhookDto = new ReqTossWebhookDto("PAYMENT_STATUS_CHANGED", "2026-05-21T00:00:00+09:00", new ReqTossWebhookDto.TossPaymentData("tgen_abc123", "order_001", "CANCELED"));
        Payment done = Payment.builder()
                .id(1L)
                .enrollmentId(1L)
                .paymentKey("tgen_abc123")
                .orderId("order_001")
                .status("DONE")
                .build();
        Enrollment confirmed = Enrollment.builder()
                .id(1L)
                .status("CONFIRMED")
                .build();

        given(paymentMapper.selectPaymentByOrderId("order_001")).willReturn(done);
        given(enrollmentMapper.selectEnrollmentById(1L)).willReturn(confirmed);
        willDoNothing().given(paymentMapper).updatePaymentCancelled(any());
        willDoNothing().given(enrollmentMapper).updateEnrollmentStatus(any());

        // when
        paymentService.handleTossWebhook(reqTossWebhookDto);

        // then
        then(paymentMapper).should().updatePaymentCancelled(any());
        then(enrollmentMapper).should().updateEnrollmentStatus(any());
    }

    @Test
    @DisplayName("웹훅 - PAYMENT_STATUS_CHANGED 아닌 이벤트 무시")
    void handleTossWebhook_ignore_otherEvent() {
        // given
        ReqTossWebhookDto reqTossWebhookDto = new ReqTossWebhookDto("PAYMENT_BILLING_KEY_ISSUED", "2026-05-21T00:00:00+09:00", null);

        // when
        paymentService.handleTossWebhook(reqTossWebhookDto);

        // then
        then(paymentMapper).should(never()).selectPaymentByOrderId(any());
    }

    @Test
    @DisplayName("웹훅 - CANCELED 아닌 상태 무시")
    void handleTossWebhook_ignore_nonCanceledStatus() {
        // given
        ReqTossWebhookDto reqTossWebhookDto = new ReqTossWebhookDto("PAYMENT_STATUS_CHANGED", "2026-05-21T00:00:00+09:00", new ReqTossWebhookDto.TossPaymentData("tgen_abc123", "order_001", "DONE"));

        // when
        paymentService.handleTossWebhook(reqTossWebhookDto);

        // then
        then(paymentMapper).should(never()).updatePaymentCancelled(any());
    }

    @Test
    @DisplayName("웹훅 - 이미 CANCELLED 상태면 멱등성 스킵")
    void handleTossWebhook_idempotent_alreadyCancelled() {
        // given
        ReqTossWebhookDto reqTossWebhookDto = new ReqTossWebhookDto("PAYMENT_STATUS_CHANGED", "2026-05-21T00:00:00+09:00", new ReqTossWebhookDto.TossPaymentData("tgen_abc123", "order_001", "CANCELED"));
        Payment cancelled = Payment.builder()
                .id(1L)
                .paymentKey("tgen_abc123")
                .orderId("order_001")
                .status("CANCELLED")
                .build();

        given(paymentMapper.selectPaymentByOrderId("order_001")).willReturn(cancelled);

        // when
        paymentService.handleTossWebhook(reqTossWebhookDto);

        // then
        then(paymentMapper).should(never()).updatePaymentCancelled(any());
    }

    @Test
    @DisplayName("웹훅 - 결제 내역 없음 - 처리 스킵")
    void handleTossWebhook_skip_paymentNotFound() {
        // given
        ReqTossWebhookDto reqTossWebhookDto = new ReqTossWebhookDto("PAYMENT_STATUS_CHANGED", "2026-05-21T00:00:00+09:00", new ReqTossWebhookDto.TossPaymentData("tgen_abc123", "order_999", "CANCELED"));

        given(paymentMapper.selectPaymentByOrderId("order_999")).willReturn(null);

        // when
        paymentService.handleTossWebhook(reqTossWebhookDto);

        // then
        then(paymentMapper).should(never()).updatePaymentCancelled(any());
    }

    @Test
    @DisplayName("웹훅 - paymentKey 불일치 - 처리 스킵")
    void handleTossWebhook_skip_paymentKeyMismatch() {
        // given
        ReqTossWebhookDto reqTossWebhookDto = new ReqTossWebhookDto("PAYMENT_STATUS_CHANGED", "2026-05-21T00:00:00+09:00", new ReqTossWebhookDto.TossPaymentData("tgen_WRONG", "order_001", "CANCELED"));
        Payment done = Payment.builder()
                .id(1L)
                .paymentKey("tgen_abc123")
                .orderId("order_001")
                .status("DONE")
                .build();

        given(paymentMapper.selectPaymentByOrderId("order_001")).willReturn(done);

        // when
        paymentService.handleTossWebhook(reqTossWebhookDto);

        // then
        then(paymentMapper).should(never()).updatePaymentCancelled(any());
    }

    @Test
    @DisplayName("웹훅 - data null - 처리 스킵")
    void handleTossWebhook_skip_dataNull() {
        // given
        ReqTossWebhookDto reqTossWebhookDto = new ReqTossWebhookDto("PAYMENT_STATUS_CHANGED", "2026-05-21T00:00:00+09:00", null);

        // when
        paymentService.handleTossWebhook(reqTossWebhookDto);

        // then
        then(paymentMapper).should(never()).updatePaymentCancelled(any());
    }

    @Test
    @DisplayName("결제 내역 조회 성공")
    void findMyPayments_success() {
        // given
        Long userId = 4L;
        Payment payment1 = Payment.builder()
                .id(1L)
                .enrollmentId(1L)
                .orderId("order_001")
                .orderName("Spring Boot 강의")
                .amount(50000)
                .method("카드")
                .status("DONE")
                .paidAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
        Payment payment2 = Payment.builder()
                .id(2L)
                .enrollmentId(2L)
                .orderId("order_002")
                .orderName("React 강의")
                .amount(30000)
                .method("카드")
                .status("CANCELLED")
                .paidAt(LocalDateTime.now().minusDays(3))
                .createdAt(LocalDateTime.now().minusDays(3))
                .build();

        ReqMyPaymentPageDto reqMyPaymentPageDto = new ReqMyPaymentPageDto();
        given(paymentMapper.selectPaymentListByUserId(reqMyPaymentPageDto)).willReturn(java.util.List.of(payment1, payment2));
        given(paymentMapper.selectPaymentListByUserIdCount(userId)).willReturn(2);

        // when
        RespPageDto<RespPaymentDto> result = paymentService.findMyPayments(userId, reqMyPaymentPageDto);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getOrderId()).isEqualTo("order_001");
        assertThat(result.getContent().get(0).getStatus()).isEqualTo("DONE");
        assertThat(result.getContent().get(1).getOrderId()).isEqualTo("order_002");
        assertThat(result.getContent().get(1).getStatus()).isEqualTo("CANCELLED");
        then(paymentMapper).should().selectPaymentListByUserId(reqMyPaymentPageDto);
        then(paymentMapper).should().selectPaymentListByUserIdCount(userId);
    }
}
