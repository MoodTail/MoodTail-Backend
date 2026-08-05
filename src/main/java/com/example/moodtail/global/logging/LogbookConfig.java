package com.example.moodtail.global.logging;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.zalando.logbook.CorrelationId;

import java.util.UUID;

@Configuration
public class LogbookConfig {

    @Bean
    public CorrelationId correlationId() {
        return request -> {
            String requestId = MDC.get(RequestMdcFilter.REQUEST_ID_MDC_KEY);
            return StringUtils.hasText(requestId) ? requestId : UUID.randomUUID().toString();
        };
    }
}
