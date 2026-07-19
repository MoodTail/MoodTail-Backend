package com.example.moodtail.global.auth.validator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PkceCodeVerifierValidator {

    private static final Pattern CODE_VERIFIER_PATTERN =
            Pattern.compile("^[A-Za-z0-9\\-._~]{43,128}$");

    public static boolean isValid(String codeVerifier) {
        return codeVerifier != null && CODE_VERIFIER_PATTERN.matcher(codeVerifier).matches();
    }
}
