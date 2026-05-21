package com.yujin.course_enrollment.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 강사 신청 거절 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReqCreatorRequestRejectDto {
    @NotBlank(message = "거절 사유를 입력해주세요.")
    private String rejectReason;
}
