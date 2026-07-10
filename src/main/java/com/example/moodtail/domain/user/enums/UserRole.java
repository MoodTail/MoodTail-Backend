package com.example.moodtail.domain.user.enums;

public enum UserRole {
    GUEST,
    USER,
    ADMIN;

    public String toAuthority() {
        return "ROLE_" + name();
    }
}
