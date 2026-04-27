package com.yujin.course_enrollment.dto.req;

import com.yujin.course_enrollment.entity.Course;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 강의 수정 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReqCourseUpdateDto {
    @NotBlank(message = "강의 제목은 필수입니다.")
    @Size(max = 100, message = "강의 제목은 100자 이하여야 합니다.")
    private String title;

    @Size(max = 3000, message = "강의 설명은 3000자 이하여야 합니다.")
    private String description;

    @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
    @Max(value = 9_999_999, message = "가격은 9,999,999원 이하여야 합니다.")
    private int price;

    @Min(value = 1, message = "정원은 1명 이상이어야 합니다.")
    @Max(value = 9_999, message = "정원은 9,999명 이하여야 합니다.")
    private int capacity;

    @NotNull(message = "수강 시작일은 필수입니다.")
    @FutureOrPresent(message = "시작일은 오늘 이후여야 합니다.")
    private LocalDate startDate;

    @NotNull(message = "수강 종료일은 필수입니다.")
    @FutureOrPresent(message = "종료일은 오늘 이후여야 합니다.")
    private LocalDate endDate;

    /* DTO → Course 엔티티 변환 */
    public Course toEntity(Long courseId, Long creatorId) {
        return Course.builder()
                .id(courseId)
                .creatorId(creatorId)
                .title(this.title)
                .description(this.description)
                .price(this.price)
                .capacity(this.capacity)
                .startDate(this.startDate)
                .endDate(this.endDate)
                .build();
    }
}