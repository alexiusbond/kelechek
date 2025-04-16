alter table student_relatives
    change work_place given_by varchar(100) null;

ALTER TABLE `lomonosov`.`student`
    ADD COLUMN `address` VARCHAR(150) NOT NULL AFTER `modification_date`;
