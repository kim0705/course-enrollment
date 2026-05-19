package com.yujin.course_enrollment.global;

/**
 * 수강 신청 상태 관리 클래스
 */
public class EnrollmentStatus {
    public static final String PENDING = "PENDING";
    public static final String CONFIRMED = "CONFIRMED";
    public static final String CANCELLED = "CANCELLED";
    public static final String WAITLIST = "WAITLIST";
    public static final String FORCE_CLOSED = "FORCE_CLOSED";

    private EnrollmentStatus() {}
}
