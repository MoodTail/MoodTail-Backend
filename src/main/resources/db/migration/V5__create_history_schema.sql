-- Core domain tables are Hibernate-managed and may be created after Flyway on an empty database.
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS drinking_records (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    cocktail_id BIGINT NOT NULL,
    record_date DATE NOT NULL,
    recorded_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_drinking_record_cocktail (cocktail_id),
    CONSTRAINT fk_drinking_record_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_drinking_record_cocktail
        FOREIGN KEY (cocktail_id) REFERENCES cocktails (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- drinking_records already exists in some environments, so CREATE TABLE IF NOT EXISTS
-- alone cannot add the one-record-per-day invariant.
SET @history_user_date_constraint_exists = (
    SELECT COUNT(*)
      FROM information_schema.TABLE_CONSTRAINTS
     WHERE CONSTRAINT_SCHEMA = DATABASE()
       AND TABLE_NAME = 'drinking_records'
       AND CONSTRAINT_NAME = 'uk_drinking_record_user_date'
       AND CONSTRAINT_TYPE = 'UNIQUE'
);
SET @add_history_user_date_constraint = IF(
    @history_user_date_constraint_exists = 0,
    'ALTER TABLE drinking_records ADD CONSTRAINT uk_drinking_record_user_date UNIQUE (user_id, record_date)',
    'SELECT 1'
);
PREPARE add_history_user_date_constraint_statement FROM @add_history_user_date_constraint;
EXECUTE add_history_user_date_constraint_statement;
DEALLOCATE PREPARE add_history_user_date_constraint_statement;

CREATE TABLE IF NOT EXISTS history_photos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    record_date DATE NOT NULL,
    image_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_history_photo_user_date (user_id, record_date),
    INDEX idx_history_photo_image (image_id),
    CONSTRAINT fk_history_photo_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_history_photo_image
        FOREIGN KEY (image_id) REFERENCES images (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET FOREIGN_KEY_CHECKS = 1;
