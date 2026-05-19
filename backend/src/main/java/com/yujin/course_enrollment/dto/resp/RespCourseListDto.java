package com.yujin.course_enrollment.dto.resp;

import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 강의 목록 응답 DTO
 */
@Getter
public class RespCourseListDto {
    private Long id;
    private Long creatorId;
    private String creatorName;
    private String title;
    private int price;
    private int capacity;
    private int enrolledCount;
    private int confirmedCount;
    private int pendingCount;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;
}