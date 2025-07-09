ALTER TABLE `ellipse`.`student_relatives`
    ADD COLUMN `issue_date` DATE NULL AFTER `given_by`,
CHANGE COLUMN `relatives_id` `relatives_id` INT NOT NULL AFTER `student_id`,
CHANGE COLUMN `is_main` `is_main` TINYINT(1) NULL DEFAULT '0' AFTER `address`,
CHANGE COLUMN `given_by` `given_by` VARCHAR(100) CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_unicode_ci' NULL DEFAULT NULL AFTER `passport`;