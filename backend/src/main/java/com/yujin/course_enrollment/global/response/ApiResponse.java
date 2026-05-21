package com.yujin.course_enrollment.global.response;

import lombok.Getter;

/**
 * 공통 응답 객체
 * 모든 API 응답을 감싸는 응답 포맷
 */
@Getter
public class ApiResponse<T> {

    private final int code;
    private final String message;
    private final T data;

    private ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /* 성공 응답 (데이터 있음) */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "success", data);
    }

    /* 성공 응답 (데이터 없음) */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(200, "success", null);
    }

    /* 생성 성공 응답 (데이터 있음) */
    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(201, "created", data);
    }

    /* 생성 성공 응답 (데이터 없음) */
    public static <T> ApiResponse<T> created() {
        return new ApiResponse<>(201, "created", null);
    }

    /* 실패 응답 */
    public static <T> ApiResponse<T> fail(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}