SET @new_history_record_constraint_exists = (
    SELECT COUNT(*)
      FROM information_schema.TABLE_CONSTRAINTS
     WHERE CONSTRAINT_SCHEMA = DATABASE()
       AND TABLE_NAME = 'drinking_records'
       AND CONSTRAINT_NAME = 'uk_drinking_record_user_date_cocktail'
       AND CONSTRAINT_TYPE = 'UNIQUE'
);
SET @add_new_history_record_constraint = IF(
    @new_history_record_constraint_exists = 0,
    'ALTER TABLE drinking_records ADD CONSTRAINT uk_drinking_record_user_date_cocktail UNIQUE (user_id, record_date, cocktail_id)',
    'SELECT 1'
);
PREPARE add_new_history_record_constraint_statement FROM @add_new_history_record_constraint;
EXECUTE add_new_history_record_constraint_statement;
DEALLOCATE PREPARE add_new_history_record_constraint_statement;

SET @old_history_record_constraint_exists = (
    SELECT COUNT(*)
      FROM information_schema.TABLE_CONSTRAINTS
     WHERE CONSTRAINT_SCHEMA = DATABASE()
       AND TABLE_NAME = 'drinking_records'
       AND CONSTRAINT_NAME = 'uk_drinking_record_user_date'
       AND CONSTRAINT_TYPE = 'UNIQUE'
);
SET @drop_old_history_record_constraint = IF(
    @old_history_record_constraint_exists > 0,
    'ALTER TABLE drinking_records DROP INDEX uk_drinking_record_user_date',
    'SELECT 1'
);
PREPARE drop_old_history_record_constraint_statement FROM @drop_old_history_record_constraint;
EXECUTE drop_old_history_record_constraint_statement;
DEALLOCATE PREPARE drop_old_history_record_constraint_statement;
