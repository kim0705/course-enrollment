package com.yujin.course_enrollment.dto.resp;

import com.yujin.course_enrollment.entity.Enrollment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 수강 신청 응답 DTO
 * 수강 신청/확정/취소 공통 응답
 */
@Getter
@Builder
@AllArgsConstructor
public class RespEnrollmentDto {
    private Long id;
    private Long userId;
    private Long courseId;
    private String courseTitle;
    private String status;
    private LocalDateTime confirmedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime createdAt;

    /* Enrollment 엔티티 + 강의 제목 → 응답 DTO 변환 */
    public static RespEnrollmentDto of(Enrollment enrollment, String courseTitle) {
        return RespEnrollmentDto.builder()
                .id(enrollment.getId())
                .userId(enrollment.getUserId())
                .courseId(enrollment.getCourseId())
                .courseTitle(courseTitle)
                .status(enrollment.getStatus())
                .confirmedAt(enrollment.getConfirmedAt())
                .cancelledAt(enrollment.getCancelledAt())
                .createdAt(enrollment.getCreatedAt())
                .build();
    }
}
