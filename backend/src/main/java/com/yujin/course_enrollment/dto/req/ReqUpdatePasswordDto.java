package com.yujin.course_enrollment.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

/**
 * 비밀번호 변경 요청 DTO
 */
@Getter
public class ReqUpdatePasswordDto {
    @NotBlank(message = "현재 비밀번호를 입력해주세요.")
    private String currentPassword;

    @NotBlank(message = "새 비밀번호를 입력해주세요.")
    @Size(min = 8, max = 16, message = "비밀번호는 8~16자여야 합니다.")
    @Pattern(
            regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?])\\S{8,16}$",
            message = "비밀번호는 영문, 숫자, 특수문자를 각각 최소 1개 이상 포함해야 합니다."
    )
    private String newPassword;
}
