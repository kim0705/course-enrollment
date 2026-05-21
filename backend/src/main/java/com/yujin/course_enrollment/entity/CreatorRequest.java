package com.yujin.course_enrollment.entity;

import com.yujin.course_enrollment.global.CreatorRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 강사 신청 엔티티
 * 수강생이 강사 계정을 신청하는 정보를 담는 객체
 * 상태: PENDING(신청 중) → APPROVED(승인) / REJECTED(거절)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatorRequest {
    private Long id;
    private Long userId;
    private String status;
    private String reason;
    private String rejectReason;
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;

    /* 강사 신청 생성 */
    public static CreatorRequest ofCreate(Long userId, String reason) {
        return CreatorRequest.builder()
                .userId(userId)
                .status(CreatorRequestStatus.PENDING)
                .reason(reason)
                .build();
    }

    /* 승인 상태로 변경할 엔티티 생성 */
    public static CreatorRequest ofApprove(Long id) {
        return CreatorRequest.builder()
                .id(id)
                .status(CreatorRequestStatus.APPROVED)
                .processedAt(LocalDateTime.now())
                .build();
    }

    /* 거절 상태로 변경할 엔티티 생성 */
    public static CreatorRequest ofReject(Long id, String rejectReason) {
        return CreatorRequest.builder()
                .id(id)
                .status(CreatorRequestStatus.REJECTED)
                .rejectReason(rejectReason)
                .processedAt(LocalDateTime.now())
                .build();
    }
}
