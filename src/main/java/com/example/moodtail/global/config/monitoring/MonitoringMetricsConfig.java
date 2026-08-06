package com.example.moodtail.global.config.monitoring;

import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MonitoringMetricsConfig {

	@Bean
	public MeterFilter excludeApiRequestMetrics() {
		return MeterFilter.deny(id -> {
			String name = id.getName();
			return name.startsWith("http.server.")
					|| name.startsWith("http.client.");
		});
	}
}
