CREATE TABLE IF NOT EXISTS local_accounts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    email VARCHAR(320) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    password_version INT NOT NULL DEFAULT 0,
    failed_login_attempts INT NOT NULL DEFAULT 0,
    locked_until DATETIME NULL,
    password_changed_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_local_account_user UNIQUE (user_id),
    CONSTRAINT uk_local_account_email UNIQUE (email),
    CONSTRAINT fk_local_account_user FOREIGN KEY (user_id) REFERENCES users (id)
);
