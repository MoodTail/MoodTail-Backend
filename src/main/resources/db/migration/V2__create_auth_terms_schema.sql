CREATE TABLE IF NOT EXISTS terms (
    id BIGINT NOT NULL AUTO_INCREMENT,
    term_type VARCHAR(20) NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    is_required BOOLEAN NOT NULL DEFAULT TRUE,
    version VARCHAR(50) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_terms_type_version UNIQUE (term_type, version)
);

CREATE TABLE IF NOT EXISTS user_term_agreements (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    term_id BIGINT NOT NULL,
    agreed_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_user_term_agreement UNIQUE (user_id, term_id),
    CONSTRAINT fk_user_term_agreement_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_term_agreement_term
        FOREIGN KEY (term_id) REFERENCES terms (id)
);
