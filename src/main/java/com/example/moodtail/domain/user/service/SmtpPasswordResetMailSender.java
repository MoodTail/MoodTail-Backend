package com.example.moodtail.domain.user.service;

import com.example.moodtail.domain.user.config.LocalAuthProperties;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.AuthErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SmtpPasswordResetMailSender implements PasswordResetMailSender {

    private final JavaMailSender mailSender;
    private final LocalAuthProperties properties;

    @Override
    public void sendCode(String recipient, String code) {
        LocalAuthProperties.PasswordReset reset = properties.passwordReset();
        if (!reset.enabled()) {
            throw new RestApiException(AuthErrorStatus.PASSWORD_RESET_DISABLED);
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(reset.sender());
        message.setTo(recipient);
        message.setSubject(reset.subject());
        message.setText(reset.bodyTemplate().formatted(code));
        try {
            mailSender.send(message);
        } catch (MailException e) {
            log.warn("Failed to send password-reset email ({})", e.getClass().getSimpleName());
            log.debug("Password-reset email failure details", e);
            throw new RestApiException(AuthErrorStatus.EMAIL_SEND_FAILED);
        }
    }
}
