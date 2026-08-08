package com.example.moodtail.global.config.monitoring;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MonitoringMetricsConfigTest {

	private final MonitoringMetricsConfig config = new MonitoringMetricsConfig();

	@Test
	void excludesApiRequestMetrics() {
		SimpleMeterRegistry registry = new SimpleMeterRegistry();
		registry.config().meterFilter(config.excludeApiRequestMetrics());

		registry.counter("http.server.requests", "method", "GET").increment();
		registry.counter("http.server.active.requests", "method", "GET").increment();
		registry.counter("http.client.requests", "method", "GET").increment();

		assertThat(registry.find("http.server.requests").counter()).isNull();
		assertThat(registry.find("http.server.active.requests").counter()).isNull();
		assertThat(registry.find("http.client.requests").counter()).isNull();
	}

	@Test
	void keepsServerLoadMetrics() {
		SimpleMeterRegistry registry = new SimpleMeterRegistry();
		registry.config().meterFilter(config.excludeApiRequestMetrics());

		registry.gauge("jvm.memory.used", 1);
		registry.gauge("hikaricp.connections.active", 1);

		assertThat(registry.find("jvm.memory.used").gauge()).isNotNull();
		assertThat(registry.find("hikaricp.connections.active").gauge()).isNotNull();
	}
}
