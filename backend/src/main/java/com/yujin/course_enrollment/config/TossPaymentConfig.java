package com.yujin.course_enrollment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * 토스페이먼츠 HTTP 클라이언트 설정
 */
@Configuration
public class TossPaymentConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
