package com.example.moodtail.domain.auth.validator;

import com.example.moodtail.global.common.exception.RestApiException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthNicknameValidatorTest {

    @Test
    void allowsOneCharacterNicknameAfterTrimming() {
        assertThat(AuthNicknameValidator.normalize(" 가 ")).isEqualTo("가");
    }

    @Test
    void allowsFiftyUnicodeCodePoints() {
        String nickname = "😀".repeat(50);

        assertThat(AuthNicknameValidator.normalize(nickname)).isEqualTo(nickname);
    }

    @Test
    void rejectsMoreThanFiftyUnicodeCodePoints() {
        assertInvalid("😀".repeat(51));
    }

    @Test
    void rejectsBlankNickname() {
        assertInvalid("   ");
    }

    @Test
    void rejectsNullNickname() {
        assertInvalid(null);
    }

    private void assertInvalid(String nickname) {
        assertThatThrownBy(() -> AuthNicknameValidator.normalize(nickname))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("USER400")
                );
    }
}
