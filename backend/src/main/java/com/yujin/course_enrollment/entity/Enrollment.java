package com.yujin.course_enrollment.entity;

import com.yujin.course_enrollment.global.EnrollmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 수강 신청 엔티티
 * 사용자가 신청한 수강 신청 정보를 담는 객체
 * 상태: PENDING(신청 완료, 결제 대기) → CONFIRMED(결제 완료, 수강 확정) → CANCELLED(취소됨)
 *       WAITLIST(대기 중) → PENDING(자동 승격)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Enrollment {
    private Long id;
    private Long userId;
    private Long courseId;
    private String status;
    private LocalDateTime confirmedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /* 결제 확정 상태로 변경할 엔티티 생성 */
    public static Enrollment ofConfirm(Long id) {
        return Enrollment.builder()
                .id(id)
                .status(EnrollmentStatus.CONFIRMED)
                .confirmedAt(LocalDateTime.now())
                .build();
    }

    /* 취소 상태로 변경할 엔티티 생성 */
    public static Enrollment ofCancel(Long id) {
        return Enrollment.builder()
                .id(id)
                .status(EnrollmentStatus.CANCELLED)
                .cancelledAt(LocalDateTime.now())
                .build();
    }

    /* 대기열 상태로 변경할 엔티티 생성 */
    public static Enrollment ofWaitlist(Long userId, Long courseId) {
        return Enrollment.builder()
                .userId(userId)
                .courseId(courseId)
                .status(EnrollmentStatus.WAITLIST)
                .build();
    }

    /* 재신청 시 PENDING 상태로 변경할 엔티티 생성 */
    public static Enrollment ofPending(Long id) {
        return Enrollment.builder()
                .id(id)
                .status(EnrollmentStatus.PENDING)
                .build();
    }

    /* 재신청 시 WAITLIST 상태로 변경할 엔티티 생성 */
    public static Enrollment ofWaitlistById(Long id) {
        return Enrollment.builder()
                .id(id)
                .status(EnrollmentStatus.WAITLIST)
                .build();
    }

    /* 강제 폐강으로 취소할 엔티티 생성 */
    public static Enrollment ofForceClose(Long id) {
        return Enrollment.builder()
                .id(id)
                .status(EnrollmentStatus.FORCE_CLOSED)
                .cancelledAt(LocalDateTime.now())
                .build();
    }
}
