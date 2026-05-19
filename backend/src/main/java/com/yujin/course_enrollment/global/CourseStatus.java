package com.yujin.course_enrollment.global;

/**
 * 강의 상태 관리 클래스
 */
public class CourseStatus {
    public static final String DRAFT = "DRAFT";
    public static final String OPEN = "OPEN";
    public static final String CLOSED = "CLOSED";
    public static final String FORCE_CLOSED = "FORCE_CLOSED";

    private CourseStatus() {}
}
