package com.yujin.course_enrollment.dto.req;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 수강 취소 요청 DTO
 */
@Getter
@NoArgsConstructor
public class ReqEnrollmentCancelDto {
    private String cancelReason;
}
