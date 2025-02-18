CREATE TABLE `employee_hide_school` (
                                        `employee_id` int NOT NULL,
                                        `school_id` int NOT NULL,
                                        KEY `employee_hide_school_employee_id_fk` (`employee_id`),
                                        KEY `employee_hide_school_school_id_fk` (`school_id`),
                                        CONSTRAINT `employee_hide_school_employee_id_fk` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`id`),
                                        CONSTRAINT `employee_hide_school_school_id_fk` FOREIGN KEY (`school_id`) REFERENCES `school` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

UPDATE `indigo`.`user_permission` SET `permissions` = 'ShowAllSchools:показ в меню' WHERE (`role_name` = 'admin') and (`permissions` = 'ChangeSchool:изменение');
UPDATE `indigo`.`user_permission` SET `permissions` = 'ShowAllSchools:показ в меню' WHERE (`role_name` = 'hr') and (`permissions` = 'ChangeSchool:изменение');
UPDATE `indigo`.`user_permission` SET `permissions` = 'ShowAllSchools:показ в меню' WHERE (`role_name` = 'sapat_secretary') and (`permissions` = 'ChangeSchool:изменение');
UPDATE `indigo`.`school` SET `is_visible` = '1' WHERE (`id` = '1');
