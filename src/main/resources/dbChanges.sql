ALTER TABLE `ellipse`.`acc_transactions`
DROP FOREIGN KEY `fk_acc_transactions_currency`;
ALTER TABLE `ellipse`.`acc_transactions`
    CHANGE COLUMN `acc_currency_id` `acc_cashbox_id` INT NOT NULL DEFAULT '4' ,
DROP INDEX `fk_acc_transactions_currency` ,
ADD INDEX `fk_acc_transactions_cashbox_idx` (`acc_cashbox_id` ASC) VISIBLE;
;
ALTER TABLE `ellipse`.`acc_transactions`
    ADD CONSTRAINT `fk_acc_transactions_cashbox`
        FOREIGN KEY (`acc_cashbox_id`)
            REFERENCES `ellipse`.`acc_cashbox` (`id`)
            ON DELETE RESTRICT;

ALTER TABLE `ellipse`.`school`
DROP COLUMN `acc_currency_id`,
DROP INDEX `fk_currency_id1_idx` ;
;

ALTER TABLE `ellipse`.`discount`
DROP FOREIGN KEY `fk_discount_currencyt1`;
ALTER TABLE `ellipse`.`discount`
DROP COLUMN `acc_currency_id`,
DROP INDEX `fk_discount_acc_currency1_idx` ;
;

USE `ellipse`;
CREATE
OR REPLACE ALGORITHM = UNDEFINED
    DEFINER = `root`@`localhost`
    SQL SECURITY DEFINER
VIEW `view_corrections` AS
SELECT GROUP_CONCAT(DISTINCT '(',
               `amr_t`.`type`,
               ') ',
               `amr_t`.`name`
                   ORDER BY `amr_t`.`id` ASC
                    SEPARATOR ', ') AS `names`,
       GROUP_CONCAT(DISTINCT '(',
               CONVERT(`amr_t`.`type` USING UTF8MB4),
               ') ',
               CONVERT(`amr_t`.`name` USING UTF8MB4),
               ' ',
               `scc`.`amount`,
               ' KGS'
                   ORDER BY `amr_t`.`id` ASC
                    SEPARATOR ', ') AS `full_details`,
       GROUP_CONCAT(DISTINCT CONVERT(`amr_t`.`type` USING UTF8MB4),
               `scc`.`amount`,
               ' KGS'
                   ORDER BY `amr_t`.`id` ASC
                    SEPARATOR ', ') AS `details`,
       GROUP_CONCAT(DISTINCT `scc`.`note`
                    ORDER BY `scc`.`id` ASC
                    SEPARATOR ', ') AS `notes`,
       `scc`.`student_id`           AS `student_id`,
       `scc`.`year_id`              AS `year_id`,
       SUM(IF((`amr_t`.`type` = '+'),
              `scc`.`amount`,
              -(`scc`.`amount`)))   AS `amount`
FROM `student_correction` `scc`
         LEFT JOIN `correction_type` `amr_t` ON ((`scc`.`correction_type_id` = `amr_t`.`id`))
         LEFT JOIN `student` `st` ON ((`st`.`id` = `scc`.`student_id`))
GROUP BY `scc`.`student_id`, `scc`.`year_id`;

DROP VIEW `ellipse`.`view_total_transactions`;



DROP TRIGGER IF EXISTS `ellipse`.`i_acc_transactions`;

DELIMITER $$
USE `ellipse`$$
CREATE DEFINER=`root`@`localhost` TRIGGER `i_acc_transactions` AFTER INSERT ON `acc_transactions` FOR EACH ROW BEGIN
INSERT INTO data_log (row_id,table_name,column_name,action, new_field,
        datetime, employee_id)
select new.id, 'transactions','row','insert',concat('category:',concat(ac.code,
                                                                       '-',ac.name),', amount:',new.amount,', cashbox:',c.name,
                                                    ', rate:',t.currency_rate, ', date:',date(t.date_time)) as newF,
       NOW(), t.employee_id
from acc_transactions as t
         left join acc_category as ac on ac.id = t.acc_category_id
         left join acc_cashbox as c on c.id = t.acc_cashbox_id
where t.id = new.id;
END$$
DELIMITER ;


DROP TRIGGER IF EXISTS `ellipse`.`d_acc_transactions`;

DELIMITER $$
USE `ellipse`$$
CREATE DEFINER=`root`@`localhost` TRIGGER `d_acc_transactions` BEFORE DELETE ON `acc_transactions` FOR EACH ROW BEGIN
INSERT INTO data_log (row_id,table_name,column_name,action, old_field,
        datetime, employee_id)
select old.id, 'transactions','row','delete',concat('category:',concat(ac.code,
                                                                       '-',ac.name),', amount:',old.amount,', cashbox:',c.name,
                                                    ', rate:',t.currency_rate, ', date:',date(t.date_time)) as oldF,
       NOW(), t.employee_id
from acc_transactions as t
         left join acc_category as ac on ac.id = t.acc_category_id
         left join acc_cashbox as c on c.id = t.acc_cashbox_id
where t.id = old.id;
END$$
DELIMITER ;


DROP TRIGGER IF EXISTS `ellipse`.`u_acc_transactions`;

DELIMITER $$
USE `ellipse`$$
CREATE DEFINER=`root`@`localhost` TRIGGER `u_acc_transactions` AFTER UPDATE ON `acc_transactions` FOR EACH ROW BEGIN
                                                                                   if (date(new.date_time)!=date(old.date_time)) then
                                                                     INSERT INTO data_log (row_id, table_name ,column_name,action,old_field, new_field,
                                                                                           datetime,employee_id)
                                                                     values(new.id,'transactions','date','update',date(old.date_time),date(new.date_time),
                                                                                   now(),new.employee_id);
end if;
if (new.amount!=old.amount) then
INSERT INTO data_log (row_id, table_name ,column_name,action,old_field, new_field,
            datetime,employee_id)
values(new.id,'transactions','amount','update',old.amount,new.amount,
            now(),new.employee_id);
end if;
        if (new.acc_cashbox_id!=old.acc_cashbox_id) then
INSERT INTO data_log (row_id,table_name,column_name,action,old_field, new_field,
            old_id,new_id,datetime,employee_id)
select new.id,'transactions','currency','update',t.name,t1.name,old.acc_cashbox_id,
       new.acc_cashbox_id, NOW(),new.employee_id
from acc_transactions as c
         left join acc_cashbox as t on t.id = old.acc_cashbox_id
         left join acc_cashbox as t1 on t1.id = new.acc_cashbox_id
where c.id=new.id;
end if;
        if (new.currency_rate!=old.currency_rate) then
INSERT INTO data_log (row_id,table_name,column_name,action,old_field, new_field,
            datetime,employee_id)
values(new.id,'transactions','currency rate','update',old.currency_rate,new.currency_rate,
            now(),new.employee_id);
end if;

        if (new.acc_category_id!=old.acc_category_id) then
INSERT INTO data_log (row_id,table_name,column_name,action,old_field, new_field,
            old_id,new_id,datetime,employee_id)
select new.id,'transactions','category','update',t.name,t1.name,old.acc_category_id,
       new.acc_category_id, NOW(),new.employee_id
from acc_transactions as c
         left join acc_category as t on t.id = old.acc_category_id
         left join acc_category as t1 on t1.id = new.acc_category_id
where c.id=new.id;
end if;

END$$
DELIMITER ;
