package com.yujin.course_enrollment.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * Jackson 전역 설정
 * 모든 @RequestBody String 필드에 앞뒤 공백 자동 제거 적용
 */
@Configuration
public class JacksonConfig {

    @Bean
    public SimpleModule stringTrimModule() {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(String.class, new StdDeserializer<>(String.class) {
            @Override
            public String deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
                String value = p.getValueAsString();
                return value != null ? value.trim() : null;
            }
        });
        return module;
    }
}
