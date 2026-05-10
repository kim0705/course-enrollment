package com.yujin.course_enrollment.dto.resp;

import com.yujin.course_enrollment.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 결제 응답 DTO
 * 결제 승인 응답
 */
@Getter
@Builder
@AllArgsConstructor
public class RespPaymentDto {
    private Long id;
    private Long enrollmentId;
    private String paymentKey;
    private String orderId;
    private String orderName;
    private int amount;
    private String method;
    private String status;
    private LocalDateTime paidAt;
    private LocalDateTime canceledAt;
    private String cancelReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /* Payment 엔티티 → 응답 DTO 변환 */
    public static RespPaymentDto of(Payment payment) {
        return RespPaymentDto.builder()
                .id(payment.getId())
                .enrollmentId(payment.getEnrollmentId())
                .paymentKey(payment.getPaymentKey())
                .orderId(payment.getOrderId())
                .orderName(payment.getOrderName())
                .amount(payment.getAmount())
                .method(payment.getMethod())
                .status(payment.getStatus())
                .paidAt(payment.getPaidAt())
                .canceledAt(payment.getCanceledAt())
                .cancelReason(payment.getCancelReason())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
