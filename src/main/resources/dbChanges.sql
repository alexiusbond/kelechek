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