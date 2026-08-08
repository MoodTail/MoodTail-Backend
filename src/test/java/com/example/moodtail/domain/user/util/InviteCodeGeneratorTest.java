package com.example.moodtail.domain.user.util;

import com.example.moodtail.domain.user.repository.UserRepository;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class InviteCodeGeneratorTest {

    private final InviteCodeGenerator inviteCodeGenerator = new InviteCodeGenerator(Mockito.mock(UserRepository.class));

    @ParameterizedTest
    @ValueSource(strings = {"MOOD-0000", "MOOD-4821", "MOOD-9999"})
    void acceptsCodesInGeneratedFormat(String inviteCode) {
        assertThat(inviteCodeGenerator.matchesFormat(inviteCode)).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"INVALID-CODE", "MOOD-123", "MOOD-12345", "mood-1234", "MOOD1234", " MOOD-1234"})
    void rejectsCodesNotInGeneratedFormat(String inviteCode) {
        assertThat(inviteCodeGenerator.matchesFormat(inviteCode)).isFalse();
    }
}
