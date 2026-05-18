package com.yujin.course_enrollment.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

/**
 * 관리자 역할 변경 요청 DTO
 */
@Getter
public class ReqAdminRoleUpdateDto {
    @NotBlank(message = "역할을 입력해주세요.")
    private String role;
}
