package com.yujin.course_enrollment.dto.req;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 수강 신청 목록 페이징 조건 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class ReqEnrollmentPageDto {
    private Long userId;
    private int page = 0;
    private int size = 5;

    /* offset 계산 */
    public int getOffset() {
        return page * size;
    }
}
