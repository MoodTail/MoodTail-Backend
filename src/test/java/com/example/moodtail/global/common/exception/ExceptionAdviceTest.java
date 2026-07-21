package com.example.moodtail.global.common.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionAdviceTest {

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
}
