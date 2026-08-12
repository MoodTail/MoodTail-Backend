package com.example.moodtail.domain.user.validator;

import com.example.moodtail.global.common.exception.RestApiException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NicknameValidatorTest {

    @Test
    void allowsOneCharacterNicknameAfterTrimming() {
        assertThat(NicknameValidator.normalize(" 가 ")).isEqualTo("가");
    }

    @Test
    void allowsFiftyUnicodeCodePoints() {
        String nickname = "😀".repeat(50);

        assertThat(NicknameValidator.normalize(nickname)).isEqualTo(nickname);
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
        assertThatThrownBy(() -> NicknameValidator.normalize(nickname))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("USER400")
                );
    }
}
