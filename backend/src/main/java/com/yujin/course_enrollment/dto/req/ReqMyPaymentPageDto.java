package com.yujin.course_enrollment.dto.req;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 나의 결제 내역 페이징 조건 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class ReqMyPaymentPageDto {
    private Long userId;
    private int page = 0;
    private int size = 10;

    /* offset 계산 */
    public int getOffset() {
        return page * size;
    }
}
