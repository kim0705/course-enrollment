package com.yujin.course_enrollment.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 결제 승인 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReqPaymentConfirmDto {
    @NotNull(message = "수강 신청 ID는 필수입니다.")
    private Long enrollmentId;

    @NotBlank(message = "결제 키는 필수입니다.")
    private String paymentKey;

    @NotBlank(message = "주문 ID는 필수입니다.")
    private String orderId;

    @NotBlank(message = "주문명은 필수입니다.")
    private String orderName;

    @Positive(message = "결제 금액은 0보다 커야 합니다.")
    private int amount;
}
