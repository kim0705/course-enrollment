package com.yujin.course_enrollment.global.response;

import lombok.Getter;

/**
 * 공통 응답 객체
 * 모든 API 응답을 감싸는 응답 포맷
 */
@Getter
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final String message;

    private ApiResponse(boolean success, T data, String message) {
        this.success = success;
        this.data = data;
        this.message = message;
    }

    /* 성공 응답 (데이터 있음) */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    /* 성공 응답 (데이터 없음) */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(true, null, null);
    }

    /* 실패 응답 */
    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>(false, null, message);
    }
}