SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS terms (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    term_type ENUM('SERVICE', 'PRIVACY', 'MARKETING') NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    is_required BOOLEAN NOT NULL DEFAULT TRUE,
    version VARCHAR(50) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_terms_type_version UNIQUE (term_type, version),
    INDEX idx_terms_active_type (is_active, term_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
