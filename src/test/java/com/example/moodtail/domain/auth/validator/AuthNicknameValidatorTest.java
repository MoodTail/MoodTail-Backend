package com.example.moodtail.domain.auth.validator;

import com.example.moodtail.global.common.exception.RestApiException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthNicknameValidatorTest {

    private static final String SUPPLEMENTARY_UNICODE_LETTER =
            new String(Character.toChars(0x10400));

    @Test
    void allowsOneCharacterNicknameAfterTrimming() {
        assertThat(AuthNicknameValidator.normalize(" 가 ")).isEqualTo("가");
    }

    @Test
    void allowsFiftyUnicodeCodePoints() {
        String nickname = SUPPLEMENTARY_UNICODE_LETTER.repeat(50);

        assertThat(AuthNicknameValidator.normalize(nickname)).isEqualTo(nickname);
    }

    @Test
    void rejectsMoreThanFiftyUnicodeCodePoints() {
        assertInvalid(SUPPLEMENTARY_UNICODE_LETTER.repeat(51));
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
