package com.yujin.course_enrollment.dto.req;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 관리자 결제 내역 페이징 조건 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class ReqAdminPaymentPageDto {
    private int page = 0;
    private int size = 10;
    private String status;

    /* offset 계산 */
    public int getOffset() {
        return page * size;
    }
}
