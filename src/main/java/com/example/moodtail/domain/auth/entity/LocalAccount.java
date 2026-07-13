package com.example.moodtail.domain.auth.entity;

import com.example.moodtail.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "local_accounts",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_local_account_user", columnNames = "user_id"),
                @UniqueConstraint(name = "uk_local_account_email", columnNames = "email")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LocalAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 320, columnDefinition = "varchar(320) collate utf8mb4_bin")
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "password_version", nullable = false)
    private int passwordVersion;

    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "password_changed_at", nullable = false)
    private LocalDateTime passwordChangedAt;

    private LocalAccount(User user, String email, String passwordHash, LocalDateTime now) {
        this.user = user;
        this.email = email;
        this.passwordHash = passwordHash;
        this.passwordVersion = 0;
        this.failedLoginAttempts = 0;
        this.passwordChangedAt = now;
    }

    public static LocalAccount create(User user, String email, String passwordHash, LocalDateTime now) {
        return new LocalAccount(user, email, passwordHash, now);
    }

    public boolean isLocked(LocalDateTime now) {
        return lockedUntil != null && lockedUntil.isAfter(now);
    }

    public void registerFailedLogin(int maxAttempts, LocalDateTime lockedUntilValue) {
        failedLoginAttempts++;
        if (failedLoginAttempts >= maxAttempts) {
            lockedUntil = lockedUntilValue;
            failedLoginAttempts = 0;
        }
    }

    public void clearLoginFailures() {
        failedLoginAttempts = 0;
        lockedUntil = null;
    }

    public void changePassword(String newPasswordHash, int expectedVersion, LocalDateTime now) {
        if (passwordVersion != expectedVersion) {
            throw new IllegalStateException("Password reset token has already been used");
        }
        passwordHash = newPasswordHash;
        passwordVersion++;
        passwordChangedAt = now;
        clearLoginFailures();
    }
}
