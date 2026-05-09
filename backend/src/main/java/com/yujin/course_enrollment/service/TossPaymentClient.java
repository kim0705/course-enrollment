package com.yujin.course_enrollment.service;

import com.yujin.course_enrollment.global.exception.BusinessException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 토스페이먼츠 결제 API 클라이언트
 * 결제 승인 API 호출을 담당
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TossPaymentClient {

    @Value("${toss.secret-key}")
    private String secretKey;

    @Value("${toss.confirm-url}")
    private String confirmUrl;

    private final RestTemplate restTemplate;

    /**
     * 토스페이먼츠 결제 승인 API 호출
     * @param paymentKey 결제 키
     * @param orderId 주문 ID
     * @param amount 결제 금액
     * @return 토스페이먼츠 결제 승인 응답
     * @throws BusinessException 토스 API 승인 실패 시 (400)
     */
    public TossConfirmResponse confirm(String paymentKey, String orderId, int amount) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8)));
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("paymentKey", paymentKey);
        body.put("orderId", orderId);
        body.put("amount", amount);

        try {
            ResponseEntity<TossConfirmResponse> response = restTemplate.exchange(confirmUrl, HttpMethod.POST, new HttpEntity<>(body, headers), TossConfirmResponse.class);
            TossConfirmResponse responseBody = response.getBody();

            if (responseBody == null) {
                log.error("[TossPaymentClient] 결제 승인 응답 body 없음 - orderId: {}", orderId);
                throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "결제 승인 응답이 비어있습니다.");
            }

            log.info("[TossPaymentClient] 결제 승인 성공 - orderId: {}", orderId);

            return responseBody;
        } catch (HttpClientErrorException e) {
            log.warn("[TossPaymentClient] 결제 승인 실패 - orderId: {}, status: {}, body: {}", orderId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new BusinessException(HttpStatus.BAD_REQUEST, "결제 승인에 실패했습니다.");
        } catch (RestClientException e) {
            log.error("[TossPaymentClient] 토스 API 통신 오류 - orderId: {}, message: {}", orderId, e.getMessage());
            throw new BusinessException(HttpStatus.BAD_GATEWAY, "결제 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 토스페이먼츠 결제 승인 응답
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TossConfirmResponse {
        private String paymentKey;
        private String orderId;
        private String orderName;
        private int totalAmount;
        private String method;
        private String status;
        private String approvedAt;

        /* ISO 8601 문자열 → LocalDateTime 변환 (e.g. "2024-01-01T10:00:00+09:00") */
        public LocalDateTime getApprovedAtAsLocalDateTime() {
            if (approvedAt == null) return null;

            return OffsetDateTime.parse(approvedAt).toLocalDateTime();
        }
    }
}
