package com.yujin.course_enrollment.dto.resp;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 강의별 수강생 목록 응답 DTO
 * 강사가 본인 강의의 수강생 목록 조회 시 사용
 */
@Getter
@Setter
@NoArgsConstructor
public class RespEnrollmentCreatorDto {
    private Long id;
    private Long userId;
    private String userName;
    private String status;
    private LocalDateTime confirmedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime createdAt;
}
