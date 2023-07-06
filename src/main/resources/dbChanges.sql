ALTER TABLE `indigo`.`student_discount`
DROP FOREIGN KEY `fk_student_discount_attechment1`;
ALTER TABLE `indigo`.`student_discount`
DROP COLUMN `attachment_id`,
DROP INDEX `fk_student_discount_attachment1_idx` ;
