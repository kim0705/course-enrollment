package com.yujin.course_enrollment.controller;

import com.yujin.course_enrollment.dto.req.ReqTossWebhookDto;
import com.yujin.course_enrollment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Toss 웹훅 컨트롤러
 * 결제 상태 변경 이벤트를 수신해 DB 상태를 동기화
 * refund() 인라인 업데이트 실패 시 결제 상태를 복구하는 안전망
 */
@Slf4j
@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class TossWebhookController {

    @Value("${toss.webhook-secret:}")
    private String webhookSecret;

    private final PaymentService paymentService;

    /**
     * Toss 웹훅 수신
     * POST /api/webhooks/toss
     * @param secret Toss 대시보드에서 설정한 웹훅 시크릿 (헤더)
     * @param reqTossWebhookDto 웹훅 페이로드
     */
    @PostMapping("/toss")
    public ResponseEntity<Void> handleTossWebhook(@RequestHeader(value = "secret", required = false) String secret, @RequestBody ReqTossWebhookDto reqTossWebhookDto) {

        // 시크릿 설정된 경우에만 검증 (상수 시간 비교로 타이밍 공격 방어)
        if (StringUtils.hasText(webhookSecret)) {
            byte[] expected = webhookSecret.getBytes(StandardCharsets.UTF_8);
            byte[] actual = (secret != null ? secret : "").getBytes(StandardCharsets.UTF_8);

            if (!MessageDigest.isEqual(expected, actual)) {
                log.warn("[TossWebhookController] 웹훅 시크릿 불일치");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        }

        log.info("[TossWebhookController] 웹훅 수신 - eventType: {}", reqTossWebhookDto.getEventType());

        try {
            paymentService.handleTossWebhook(reqTossWebhookDto);
        } catch (Exception e) {
            log.error("[TossWebhookController] 웹훅 처리 실패 - eventType: {}", reqTossWebhookDto.getEventType(), e);
        }

        return ResponseEntity.ok().build();
    }
}
