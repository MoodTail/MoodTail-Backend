package com.example.moodtail.domain.user.service;

public interface PasswordResetMailSender {

    void sendCode(String recipient, String code);
}
