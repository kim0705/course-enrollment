package com.yujin.course_enrollment.global.exception;

import com.yujin.course_enrollment.global.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 전역 예외 처리기
 * 컨트롤러에서 발생하는 예외를 ApiResponse 포맷으로 통일하여 응답
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 중복 수강 신청 처리 (동시 요청으로 UNIQUE 제약 위반 시)
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ApiResponse<?>> handleDuplicateKeyException(DuplicateKeyException e) {
        log.warn("[GlobalExceptionHandler] DuplicateKeyException: {}", e.getMessage());

        return ResponseEntity.badRequest().body(ApiResponse.fail(400, "이미 신청한 강의입니다."));
    }

    /**
     * 비즈니스 로직 예외 처리
     * BusinessException이 가진 HTTP 상태 코드로 응답
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<?>> handleBusinessException(BusinessException e) {
        log.warn("[GlobalExceptionHandler] BusinessException: {}", e.getMessage());

        return ResponseEntity.status(e.getStatus()).body(ApiResponse.fail(e.getStatus().value(), e.getMessage()));
    }

    /**
     * 요청 바디 유효성 검증 실패 처리 (@Valid)
     * 첫 번째 필드 오류 메시지를 응답
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse("입력값이 올바르지 않습니다.");
        log.warn("[GlobalExceptionHandler] ValidationException: {}", message);

        return ResponseEntity.badRequest().body(ApiResponse.fail(400, message));
    }

    /**
     * 필수 요청 헤더 누락 처리
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiResponse<?>> handleMissingHeaderException(MissingRequestHeaderException e) {
        String message = e.getHeaderName() + " 헤더가 누락되었습니다.";
        log.warn("[GlobalExceptionHandler] MissingRequestHeaderException: {}", message);

        return ResponseEntity.badRequest().body(ApiResponse.fail(400, message));
    }

    /**
     * 요청 바디 파싱 실패 처리 (잘못된 JSON 형식, 타입 불일치 등)
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("[GlobalExceptionHandler] HttpMessageNotReadableException: {}", e.getMessage());

        return ResponseEntity.badRequest().body(ApiResponse.fail(400, "요청 형식이 올바르지 않습니다."));
    }

    /**
     * 그 외 예상치 못한 서버 오류 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleException(Exception e) {
        log.error("[GlobalExceptionHandler] Unexpected Exception", e);

        return ResponseEntity.internalServerError().body(ApiResponse.fail(500, "서버 오류가 발생했습니다."));
    }
}
