package com.yujin.course_enrollment.dto.req;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Toss 웹훅 수신 DTO
 * 결제 상태 변경 이벤트 (PAYMENT_STATUS_CHANGED) 처리
 */
@Getter
@NoArgsConstructor
public class ReqTossWebhookDto {
    private String eventType;
    private String createdAt;
    private TossPaymentData data;

    @Getter
    @NoArgsConstructor
    public static class TossPaymentData {
        private String paymentKey;
        private String orderId;
        private String status;
    }

}