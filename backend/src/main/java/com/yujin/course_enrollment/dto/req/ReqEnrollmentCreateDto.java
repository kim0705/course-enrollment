package com.yujin.course_enrollment.dto.req;

import com.yujin.course_enrollment.entity.Enrollment;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 수강 신청 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReqEnrollmentCreateDto {
    @NotNull(message = "강의 ID는 필수입니다.")
    private Long courseId;

    /* DTO → Enrollment 엔티티 변환 */
    public Enrollment toEntity(Long userId) {
        return Enrollment.builder()
                .userId(userId)
                .courseId(this.courseId)
                .build();
    }
}
