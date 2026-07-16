CREATE TABLE IF NOT EXISTS account_withdrawal_asset_cleanup (
    image_id BIGINT NOT NULL,
    image_url VARCHAR(1024) NOT NULL,
    attempt_count INT NOT NULL DEFAULT 0,
    next_attempt_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (image_id),
    INDEX idx_account_withdrawal_asset_cleanup_due (next_attempt_at)
);
