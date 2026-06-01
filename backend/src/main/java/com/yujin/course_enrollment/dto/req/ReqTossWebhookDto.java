package com.yujin.course_enrollment.dto.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

/**
 * Toss 웹훅 수신 DTO
 * 결제 상태 변경 이벤트 (PAYMENT_STATUS_CHANGED) 처리
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReqTossWebhookDto {
    private String eventType;
    private String createdAt;
    private TossPaymentData data;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TossPaymentData {
        private String paymentKey;
        private String orderId;
        private String status;
        private String method;
        private String approvedAt;

        /* ISO 8601 문자열 → LocalDateTime 변환 (e.g. "2024-01-01T10:00:00+09:00") */
        public LocalDateTime getApprovedAtAsLocalDateTime() {
            if (approvedAt == null) return null;

            return OffsetDateTime.parse(approvedAt).toLocalDateTime();
        }
    }
}
