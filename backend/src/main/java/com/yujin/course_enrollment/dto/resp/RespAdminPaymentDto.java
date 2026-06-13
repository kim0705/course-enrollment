package com.yujin.course_enrollment.dto.resp;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 관리자 결제 내역 응답 DTO
 */
@Getter
public class RespAdminPaymentDto {
    private Long id;
    private Long enrollmentId;
    private String userName;
    private String courseName;
    private String orderId;
    private String orderName;
    private int amount;
    private String method;
    private String status;
    private LocalDateTime paidAt;
    private LocalDateTime canceledAt;
    private String cancelReason;
    private LocalDateTime createdAt;
}
