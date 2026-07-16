package com.example.moodtail.domain.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AccountWithdrawalService {

    void withdraw(Long userId, HttpServletRequest request, HttpServletResponse response);
}
