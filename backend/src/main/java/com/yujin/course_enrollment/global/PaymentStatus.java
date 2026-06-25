package com.yujin.course_enrollment.global;

/**
 * 결제 상태 관리 클래스
 */
public class PaymentStatus {
    public static final String PENDING = "PENDING";
    public static final String DONE = "DONE";
    public static final String FAILED = "FAILED";
    public static final String CANCELLED = "CANCELLED";
    public static final String REFUND_FAILED = "REFUND_FAILED";

    private PaymentStatus() {}
}
