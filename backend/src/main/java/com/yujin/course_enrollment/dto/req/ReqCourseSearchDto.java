package com.yujin.course_enrollment.dto.req;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 강의 목록 검색 조건 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class ReqCourseSearchDto {
    private String status;
    private String searchType;
    private String keyword;
    private int page = 0;
    private int size = 10;

    /* offset 계산 */
    public int getOffset() {
        return page * size;
    }
}