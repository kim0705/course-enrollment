package com.yujin.course_enrollment.dto.resp;

import com.yujin.course_enrollment.entity.User;
import lombok.Builder;
import lombok.Getter;

/**
 * 로그인 응답 DTO
 */
@Getter
@Builder
public class RespLoginDto {
    private Long id;
    private String username;
    private String name;
    private String role;

    /* User 엔티티 → 로그인 응답 DTO 변환 */
    public static RespLoginDto from(User user) {
        return RespLoginDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .role(user.getRole())
                .build();
    }
}
