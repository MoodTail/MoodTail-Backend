SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS inquiries (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,
    contact_email VARCHAR(255) NULL,
    inquiry_type ENUM('BUG', 'FEEDBACK', 'ACCOUNT', 'ETC') NOT NULL,
    content TEXT NOT NULL,
    status ENUM('PENDING', 'RESOLVED') NOT NULL DEFAULT 'PENDING',
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_inquiries_user
        FOREIGN KEY (user_id) REFERENCES users (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
