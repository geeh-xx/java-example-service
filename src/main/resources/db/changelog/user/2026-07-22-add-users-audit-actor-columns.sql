ALTER TABLE users
    ADD COLUMN created_by VARCHAR(64) NULL AFTER created_date,
    ADD COLUMN updated_by VARCHAR(64) NULL AFTER last_modified_date;
