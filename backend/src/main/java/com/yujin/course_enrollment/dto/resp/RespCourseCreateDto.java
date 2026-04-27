package com.yujin.course_enrollment.dto.resp;

import com.yujin.course_enrollment.entity.Course;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 강의 등록 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
public class RespCourseCreateDto {
        private Long id;
        private Long creatorId;
        private String title;
        private String description;
        private int price;
        private int capacity;
        private String status;
        private LocalDate startDate;
        private LocalDate endDate;
        private LocalDateTime createdAt;

        /* Course 엔티티 → 응답 DTO 변환 */
        public static RespCourseCreateDto from(Course course) {
                return RespCourseCreateDto.builder()
                        .id(course.getId())
                        .creatorId(course.getCreatorId())
                        .title(course.getTitle())
                        .description(course.getDescription())
                        .price(course.getPrice())
                        .capacity(course.getCapacity())
                        .status(course.getStatus())
                        .startDate(course.getStartDate())
                        .endDate(course.getEndDate())
                        .createdAt(course.getCreatedAt())
                        .build();
        }
}