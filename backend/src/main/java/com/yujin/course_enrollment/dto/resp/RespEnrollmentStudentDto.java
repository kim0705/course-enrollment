package com.yujin.course_enrollment.dto.resp;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 수강 신청 목록 응답 DTO
 * 수강 신청 정보와 강의 요약 정보를 함께 반환
 */
@Getter
@Setter
@NoArgsConstructor
public class RespEnrollmentStudentDto {
    private Long id;
    private Long courseId;
    private String courseTitle;
    private String courseStatus;
    private int price;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private LocalDateTime confirmedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime createdAt;
}
