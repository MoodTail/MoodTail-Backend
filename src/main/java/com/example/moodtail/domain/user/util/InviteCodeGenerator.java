package com.example.moodtail.domain.user.util;

import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.global.common.exception.RestApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.regex.Pattern;

import static com.example.moodtail.global.common.exception.code.status.UserErrorStatus.INVITE_CODE_GENERATION_FAILED;

@Component
@RequiredArgsConstructor
public class InviteCodeGenerator {

    private static final String CODE_PREFIX = "MOOD-";
    private static final int CODE_NUMBER_BOUND = 10_000;
    private static final int MAX_GENERATION_ATTEMPTS = 10;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Pattern INVITE_CODE_PATTERN = Pattern.compile("^" + CODE_PREFIX + "\\d{4}$");

    private final UserRepository userRepository;

    public boolean matchesFormat(String inviteCode) {
        return inviteCode != null && INVITE_CODE_PATTERN.matcher(inviteCode).matches();
    }

    public String generateUnique() {
        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            String candidate = generateCandidate();
            if (userRepository.findByInviteCode(candidate).isEmpty()) {
                return candidate;
            }
        }
        throw new RestApiException(INVITE_CODE_GENERATION_FAILED);
    }

    private String generateCandidate() {
        int number = SECURE_RANDOM.nextInt(CODE_NUMBER_BOUND);
        return CODE_PREFIX + String.format("%04d", number);
    }
}
