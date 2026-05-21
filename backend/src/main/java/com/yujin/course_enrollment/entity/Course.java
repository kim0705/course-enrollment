package com.yujin.course_enrollment.entity;

import com.yujin.course_enrollment.global.CourseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 강의 엔티티
 * 크리에이터가 개설한 강의 정보를 담는 객체
 * 상태: DRAFT(초안) → OPEN(모집 중) → CLOSED(모집 마감)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Course {
    private Long id;
    private Long creatorId;
    private String title;
    private String description;
    private int price;
    private int capacity;
    private int enrolledCount;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /* 강의 공개 상태 변경용 */
    public static Course ofOpen(Long id) {
        return Course.builder()
                .id(id)
                .status(CourseStatus.OPEN)
                .build();
    }

    /* 강의 마감 상태 변경용 */
    public static Course ofClosed(Long id) {
        return Course.builder()
                .id(id)
                .status(CourseStatus.CLOSED)
                .build();
    }

    /* 강제 폐강 상태 변경용 */
    public static Course ofForceClosed(Long id) {
        return Course.builder()
                .id(id)
                .status(CourseStatus.FORCE_CLOSED)
                .build();
    }
}