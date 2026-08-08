package com.example.moodtail.global.common.exception;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.example.moodtail.global.common.exception.code.status.GlobalErrorStatus;
import com.example.moodtail.global.common.exception.code.status.WeatherErrorStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionAdviceTest {

    private final Logger logger = (Logger) LoggerFactory.getLogger(ExceptionAdvice.class);
    private ListAppender<ILoggingEvent> appender;

    @BeforeEach
    void attachAppender() {
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void detachAppender() {
        logger.detachAppender(appender);
        appender.stop();
    }

    @Test
    void doesNotExposeUnexpectedExceptionDetails() {
        var response = new ExceptionAdvice().handleException(
                new IllegalStateException("database-password=secret")
        );

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("COMMON500");
        assertThat(response.getBody().getResult()).isNull();
    }

    @Test
    void logsHandledServerExceptionAtErrorLevel() {
        RestApiException exception = new RestApiException(
                WeatherErrorStatus.WEATHER_CONFIGURATION_ERROR
        );

        var response = new ExceptionAdvice().handleRestApiException(exception);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(appender.list).singleElement().satisfies(event -> {
            assertThat(event.getLevel()).isEqualTo(Level.ERROR);
            assertThat(event.getFormattedMessage())
                    .contains("WEATHER500")
                    .contains("500");
            assertThat(event.getThrowableProxy()).isNotNull();
        });
    }

    @Test
    void doesNotLogExpectedClientExceptionAtErrorLevel() {
        RestApiException exception = new RestApiException(GlobalErrorStatus._BAD_REQUEST);

        var response = new ExceptionAdvice().handleRestApiException(exception);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(appender.list).isEmpty();
    }
}
