package com.example.moodtail.domain.history.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class HistoryTimeConfiguration {

    @Bean
    public Clock historyClock() {
        return Clock.system(ZoneId.of("Asia/Seoul"));
    }
}
