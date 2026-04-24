package com.yujin.course_enrollment.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 강의 상세 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RespCourseDetailDto {
    private Long id;
    private Long creatorId;
    private String creatorName;
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
    private boolean enrolled;
}