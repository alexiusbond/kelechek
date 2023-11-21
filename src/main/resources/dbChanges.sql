USE `indigo`;
CREATE
OR REPLACE ALGORITHM = UNDEFINED
    DEFINER = `root`@`localhost`
    SQL SECURITY DEFINER
VIEW `view_corrections` AS
SELECT
    GROUP_CONCAT(DISTINCT '(',
            `amr_t`.`type`,
            ') ',
            `amr_t`.`name`
                ORDER BY `amr_t`.`id` ASC
            SEPARATOR ', ') AS `names`,
    GROUP_CONCAT(DISTINCT '(',
            `amr_t`.`type`,
            ') ',
            `amr_t`.`name`,
            ' ',
            `scc`.`amount`,
            ' $'
                ORDER BY `amr_t`.`id` ASC
            SEPARATOR ', ') AS `full_details`,
    GROUP_CONCAT(DISTINCT `amr_t`.`type`,
            `scc`.`amount`,
            ' ',
            cur.name
                ORDER BY `amr_t`.`id` ASC
            SEPARATOR ', ') AS `details`,
    GROUP_CONCAT(DISTINCT `scc`.`note`
            ORDER BY `scc`.`id` ASC
            SEPARATOR ', ') AS `notes`,
    `scc`.`student_id` AS `student_id`,
    `scc`.`year_id` AS `year_id`,
    SUM(IF((`amr_t`.`type` = '+'),
           `scc`.`amount`,
           -(`scc`.`amount`))) AS `amount`
FROM
    (`student_correction` `scc`
        LEFT JOIN `correction_type` `amr_t` ON ((`scc`.`correction_type_id` = `amr_t`.`id`))
        LEFT JOIN student `st` ON st.id = scc.student_id
        LEFT JOIN school AS sc ON sc.id = st.school_id
        LEFT JOIN acc_currency AS cur ON cur.id = sc.acc_currency_id)
GROUP BY `scc`.`student_id` , `scc`.`year_id`;

ALTER TABLE `indigo`.`discount`
    ADD COLUMN `acc_currency_id` INT NULL DEFAULT NULL AFTER `discount_unit_id`;
ALTER TABLE `indigo`.`discount`
    ADD INDEX `fk_discount_acc_currency1_idx` (`acc_currency_id` ASC) VISIBLE;
ALTER TABLE `indigo`.`discount`
    ADD CONSTRAINT `fk_discount_currencyt1`
        FOREIGN KEY (`acc_currency_id`)
            REFERENCES `indigo`.`acc_currency` (`id`)
            ON DELETE RESTRICT
            ON UPDATE NO ACTION;


UPDATE indigo.discount
SET
    acc_currency_id = 2
WHERE
        discount_type_id IN (2 , 4);

update school set primary_code = code, secondary_code = code;

UPDATE `indigo`.`class_type` SET `order_number_min` = '0' WHERE (`id` = '4');
UPDATE `indigo`.`class_type` SET `order_number_min` = '700' WHERE (`id` = '3');
UPDATE `indigo`.`class_type` SET `order_number_max` = '699' WHERE (`id` = '2');
