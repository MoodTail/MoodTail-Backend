package com.example.moodtail.global.logging;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.io.ClassPathResource;
import org.zalando.logbook.BodyFilter;
import org.zalando.logbook.HeaderFilter;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.QueryFilter;
import org.zalando.logbook.autoconfigure.LogbookAutoConfiguration;
import org.zalando.logbook.autoconfigure.LogbookProperties;

import static org.assertj.core.api.Assertions.assertThat;

class HttpLoggingConfigurationTest {

    private static final String MASKED = "<masked>";

    private final LogbookProperties properties = loadProperties();
    private final LogbookAutoConfiguration autoConfiguration = new LogbookAutoConfiguration(properties);

    @Test
    void masksAuthenticationHeaders() {
        HeaderFilter filter = autoConfiguration.headerFilter();
        HttpHeaders headers = HttpHeaders.of("Authorization", "Bearer secret")
                .update("Cookie", "refreshToken=secret")
                .update("X-Visible", "visible");

        HttpHeaders filtered = filter.filter(headers);

        assertThat(filtered.getFirst("Authorization")).isEqualTo(MASKED);
        assertThat(filtered.getFirst("Cookie")).isEqualTo(MASKED);
        assertThat(filtered.getFirst("X-Visible")).isEqualTo("visible");
    }

    @Test
    void masksSensitiveJsonFields() {
        BodyFilter filter = autoConfiguration.jsonBodyFieldsFilter();
        String body = """
                {"code":"COMMON200","email":"user@example.com","password":"secret","accessToken":"token","visible":"value"}
                """;

        String filtered = filter.filter("application/json", body);

        assertThat(filtered)
                .doesNotContain("user@example.com", "secret", "token")
                .contains("\"code\":\"COMMON200\"")
                .contains("\"email\":\"<masked>\"")
                .contains("\"visible\":\"value\"");
    }

    @Test
    void masksSensitiveQueryParameters() {
        QueryFilter filter = autoConfiguration.queryFilter();

        String filtered = filter.filter("email=user%40example.com&code=123456&visible=value");

        assertThat(filtered)
                .doesNotContain("user%40example.com", "123456")
                .contains("visible=value");
    }

    @Test
    void limitsLoggedBodyAndScopesTrafficToApplicationPaths() {
        assertThat(properties.getWrite().getMaxBodySize()).isEqualTo(16_384);
        assertThat(properties.getPredicate().getInclude())
                .extracting(LogbookProperties.LogbookPredicate::getPath)
                .containsExactly("/api/**", "/share/**");
        assertThat(properties.getPredicate().getExclude()).singleElement().satisfies(exclude -> {
            assertThat(exclude.getPath()).isEqualTo("/**");
            assertThat(exclude.getMethods()).containsExactly("OPTIONS");
        });
    }

    private LogbookProperties loadProperties() {
        try {
            MutablePropertySources propertySources = new MutablePropertySources();
            new YamlPropertySourceLoader()
                    .load("application", new ClassPathResource("application.yaml"))
                    .forEach(propertySources::addLast);
            return new Binder(ConfigurationPropertySources.from(propertySources))
                    .bind("logbook", Bindable.of(LogbookProperties.class))
                    .orElseThrow(() -> new IllegalStateException("Logbook configuration is missing"));
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to load Logbook configuration", exception);
        }
    }
}
