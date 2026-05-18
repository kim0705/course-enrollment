package com.yujin.course_enrollment.dto.resp;

import lombok.Builder;
import lombok.Getter;

/**
 * 관리자 대시보드 통계 응답 DTO
 */
@Getter
@Builder
public class RespAdminDashboardDto {
    private int totalUsers;
    private int studentCount;
    private int creatorCount;
    private int totalCourses;
    private int draftCount;
    private int openCount;
    private int closedCount;
    private int totalEnrollments;

    /* 통계 집계 결과로 대시보드 응답 생성 */
    public static RespAdminDashboardDto of(int totalUsers, int studentCount, int creatorCount, int totalCourses, int draftCount, int openCount, int closedCount, int totalEnrollments) {
        return RespAdminDashboardDto.builder()
                .totalUsers(totalUsers)
                .studentCount(studentCount)
                .creatorCount(creatorCount)
                .totalCourses(totalCourses)
                .draftCount(draftCount)
                .openCount(openCount)
                .closedCount(closedCount)
                .totalEnrollments(totalEnrollments)
                .build();
    }
}
