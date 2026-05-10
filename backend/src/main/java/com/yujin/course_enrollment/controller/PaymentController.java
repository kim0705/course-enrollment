package com.yujin.course_enrollment.controller;

import com.yujin.course_enrollment.dto.req.ReqPaymentConfirmDto;
import com.yujin.course_enrollment.dto.resp.RespPaymentDto;
import com.yujin.course_enrollment.global.response.ApiResponse;
import com.yujin.course_enrollment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 결제 컨트롤러
 * 토스페이먼츠 결제 승인 관련 HTTP 요청을 처리
 */
@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 토스페이먼츠 결제 승인
     * POST /api/payments/confirm
     * @param userId 사용자 ID (헤더로 전달)
     * @param reqPaymentConfirmDto 결제 승인 요청 DTO (enrollmentId, paymentKey, orderId, orderName, amount)
     * @return 저장된 결제 응답 DTO
     */
    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<RespPaymentDto>> confirmPayment(@RequestHeader("X-User-Id") Long userId, @Valid @RequestBody ReqPaymentConfirmDto reqPaymentConfirmDto) {
        log.debug("[PaymentController] 결제 승인 요청 - userId: {}, enrollmentId: {}", userId, reqPaymentConfirmDto.getEnrollmentId());

        RespPaymentDto result = paymentService.confirmPayment(userId, reqPaymentConfirmDto);

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 나의 결제 내역 조회
     * GET /api/payments/my
     * @param userId 사용자 ID (헤더로 전달)
     * @return 결제 내역 목록 (DONE, CANCELLED)
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<RespPaymentDto>>> getMyPayments(@RequestHeader("X-User-Id") Long userId) {
        log.debug("[PaymentController] 결제 내역 조회 요청 - userId: {}", userId);

        List<RespPaymentDto> result = paymentService.findMyPayments(userId);

        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
