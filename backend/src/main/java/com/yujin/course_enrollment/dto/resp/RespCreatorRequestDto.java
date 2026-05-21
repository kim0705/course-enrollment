package com.yujin.course_enrollment.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 강사 신청 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RespCreatorRequestDto {
    private Long id;
    private Long userId;
    private String username;
    private String name;
    private String status;
    private String reason;
    private String rejectReason;
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;
}
