package com.example.moodtail.domain.user.entity;

public enum UserRole {
    USER,
    GUEST,
    ADMIN;

    public String toAuthority() {
        return "ROLE_" + name();
    }
}
