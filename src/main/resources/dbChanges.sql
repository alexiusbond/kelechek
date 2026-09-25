ALTER TABLE `kelechek`.`student_relatives`
    ADD COLUMN `work_place` VARCHAR(100) NULL DEFAULT NULL AFTER `is_main`,
ADD COLUMN `passport` VARCHAR(45) NULL DEFAULT NULL AFTER `work_place`,
ADD COLUMN `passport_issue_date` DATE NULL DEFAULT NULL AFTER `passport`,
ADD COLUMN `passport_issue_place` VARCHAR(45) NULL DEFAULT NULL AFTER `passport_issue_date`;
