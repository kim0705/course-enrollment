package com.yujin.course_enrollment.entity;

import com.yujin.course_enrollment.global.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 결제 엔티티
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
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

    /* 토스 API 호출 전 결제 선저장용 엔티티 생성 */
    public static Payment ofPending(Long enrollmentId, String orderId, String orderName, int amount) {
        return Payment.builder()
                .enrollmentId(enrollmentId)
                .orderId(orderId)
                .orderName(orderName)
                .amount(amount)
                .status(PaymentStatus.PENDING)
                .build();
    }

    /* 토스 승인 완료 후 DONE 업데이트용 엔티티 생성 */
    public static Payment ofDone(Long id, String paymentKey, String method, LocalDateTime paidAt) {
        return Payment.builder()
                .id(id)
                .paymentKey(paymentKey)
                .method(method)
                .status(PaymentStatus.DONE)
                .paidAt(paidAt)
                .build();
    }

    /* 환불 완료 후 CANCELLED 업데이트용 엔티티 생성 */
    public static Payment ofCancelled(Long id, String cancelReason) {
        return Payment.builder()
                .id(id)
                .status(PaymentStatus.CANCELLED)
                .canceledAt(LocalDateTime.now())
                .cancelReason(cancelReason)
                .build();
    }
}
