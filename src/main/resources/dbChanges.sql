ALTER TABLE `star`.`student_relatives`
DROP FOREIGN KEY `fk_student_relatives_attachment`;
ALTER TABLE `star`.`student_relatives`
DROP COLUMN `attachment_id`,
CHANGE COLUMN `inn` `work_place` VARCHAR(250) NULL DEFAULT NULL ,
DROP INDEX `fk_student_relatives_attachment_idx` ;