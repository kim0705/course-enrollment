package com.yujin.course_enrollment.dto.req;

import com.yujin.course_enrollment.entity.Course;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 강의 등록 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReqCourseCreateDto {
    @NotBlank(message = "강의 제목은 필수입니다.")
    @Size(max = 100, message = "강의 제목은 100자 이하여야 합니다.")
    private String title;

    private String description;

    @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
    private int price;

    @Min(value = 1, message = "정원은 1명 이상이어야 합니다.")
    private int capacity;

    @NotNull(message = "수강 시작일은 필수입니다.")
    private LocalDate startDate;

    @NotNull(message = "수강 종료일은 필수입니다.")
    private LocalDate endDate;

    /* DTO → Course 엔티티 변환 */
    public Course toEntity(Long creatorId) {
        return Course.builder()
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