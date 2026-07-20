ALTER TABLE users
    ADD COLUMN invite_code VARCHAR(20) NULL,
    ADD CONSTRAINT uk_users_invite_code UNIQUE (invite_code);
