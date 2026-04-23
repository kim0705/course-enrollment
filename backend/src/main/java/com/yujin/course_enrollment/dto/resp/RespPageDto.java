package com.yujin.course_enrollment.dto.resp;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 페이징 응답 DTO
 */
@Getter
@Builder
public class RespPageDto<T> {
    private List<T> content;
    private int page;
    private int size;
    private int totalCount;
    private int totalPages;
    private boolean last;

    /* 페이징 응답 생성 */
    public static <T> RespPageDto<T> of(List<T> content, int page, int size, int totalCount) {
        int totalPages = (int) Math.ceil((double) totalCount / size);

        return RespPageDto.<T>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalCount(totalCount)
                .totalPages(totalPages)
                .last(page >= totalPages - 1)
                .build();
    }
}