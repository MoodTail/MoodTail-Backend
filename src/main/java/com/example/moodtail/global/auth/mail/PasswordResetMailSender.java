package com.example.moodtail.global.auth.mail;

public interface PasswordResetMailSender {

    void sendCode(String recipient, String code);
}
