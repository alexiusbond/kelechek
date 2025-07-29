use ellipse;

drop table if exists students_2022;

-- 1) Создаём таблицу
CREATE TABLE `students_2022` (
                                 `login`       INT UNSIGNED   NOT NULL COMMENT 'Логин ученика',
                                 `class_name`  VARCHAR(10)    NOT NULL COMMENT 'Класс (например 4р,7к и т.п.)',
                                 `surname`     VARCHAR(100)   NOT NULL COMMENT 'Фамилия',
                                 `name`        VARCHAR(100)   NOT NULL COMMENT 'Имя',
                                 `contract`    INT UNSIGNED   NOT NULL COMMENT 'Сумма по контракту',
                                 `gender`      TINYINT        NOT NULL COMMENT '1=мужской, 2=женский',
                                 PRIMARY KEY (`login`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

-- 2) Загружаем данные из CSV
-- Скопируйте файл students_2022.csv в директорию, доступную MySQL (например /var/lib/mysql-files/)
LOAD DATA LOCAL INFILE 'C:\\ProgramData\\MySQL\\MySQL Server 8.0\\Uploads\\students_2022.csv'
INTO TABLE students_2022
CHARACTER SET utf8mb4
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\r\n'
IGNORE 1 LINES
  (`login`,`class_name`,`surname`,`name`,`contract`,`gender`);

insert into student
SELECT NULL, t.login, '-', t.name, t.surname, null, NOW(), NULL, 44, t.gender, 8, 1, NOW(), '-'  FROM ellipse.students_2022 as t;

INSERT INTO `ellipse`.`class_name` (`name`, `school_id`, `class_number_id`, `activity_status_id`, `class_type_id`) VALUES ('п', '44', '1', '2', '1');
INSERT INTO `ellipse`.`class_name` (`name`, `school_id`, `class_number_id`, `activity_status_id`, `class_type_id`) VALUES ('к', '44', '2', '2', '3');
INSERT INTO `ellipse`.`class_name` (`name`, `school_id`, `class_number_id`, `activity_status_id`, `class_type_id`) VALUES ('к', '44', '3', '2', '3');
INSERT INTO `ellipse`.`class_name` (`name`, `school_id`, `class_number_id`, `activity_status_id`, `class_type_id`) VALUES ('р', '44', '4', '2', '2');
INSERT INTO `ellipse`.`class_name` (`name`, `school_id`, `class_number_id`, `activity_status_id`, `class_type_id`) VALUES ('р', '44', '5', '2', '2');
INSERT INTO `ellipse`.`class_name` (`name`, `school_id`, `class_number_id`, `activity_status_id`, `class_type_id`) VALUES ('к', '44', '6', '2', '3');
INSERT INTO `ellipse`.`class_name` (`name`, `school_id`, `class_number_id`, `activity_status_id`, `class_type_id`) VALUES ('к', '44', '8', '2', '3');
INSERT INTO `ellipse`.`class_name` (`name`, `school_id`, `class_number_id`, `activity_status_id`, `class_type_id`) VALUES ('р', '44', '8', '2', '2');
INSERT INTO `ellipse`.`class_name` (`name`, `school_id`, `class_number_id`, `activity_status_id`, `class_type_id`) VALUES ('выбывшие 2022', '44', '1', '2', '1');
INSERT INTO `ellipse`.`class_name` (`name`, `school_id`, `class_number_id`, `activity_status_id`, `class_type_id`) VALUES ('р', '44', '2', '2', '2');
INSERT INTO `ellipse`.`class_name` (`id`, `name`, `school_id`, `class_number_id`, `activity_status_id`, `class_type_id`) VALUES (NULL, 'р', '44', '3', '2', '2');
INSERT INTO `ellipse`.`class_name` (`name`, `school_id`, `class_number_id`, `activity_status_id`, `class_type_id`) VALUES ('к', '44', '4', '2', '3');
INSERT INTO `ellipse`.`class_name` (`name`, `school_id`, `class_number_id`, `activity_status_id`, `class_type_id`) VALUES ('р', '44', '6', '2', '2');
INSERT INTO `ellipse`.`class_name` (`name`, `school_id`, `class_number_id`, `activity_status_id`, `class_type_id`) VALUES ('р', '44', '7', '2', '2');
INSERT INTO `ellipse`.`class_name` (`name`, `school_id`, `class_number_id`, `activity_status_id`, `class_type_id`) VALUES ('к', '44', '7', '2', '3');
INSERT INTO `ellipse`.`class_name` (`name`, `school_id`, `class_number_id`, `activity_status_id`, `class_type_id`) VALUES ('р', '44', '9', '2', '2');

insert into student_orders
SELECT NULL,
       (SELECT
            id
        FROM
            student
        WHERE
            login = t.login),
       4,
       200,
       (SELECT
            cn.id
        FROM
            class_name AS cn
                LEFT JOIN
            class_number AS n ON cn.class_number_id = n.id
        WHERE
            CONCAT(n.name, cn.name) = t.class_name), 1, 1, 8, NULL, NOW(), 1, 1
FROM
    students_2022 AS t;

INSERT INTO `ellipse`.`contract` (`name`, `amount`, `year_id`, `school_id`, `activity_status_id`, `employee_id`) VALUES ('Для подкурса 2022', '144000', '8', '44', '2', '1');
INSERT INTO `ellipse`.`contract` (`name`, `amount`, `year_id`, `school_id`, `activity_status_id`, `employee_id`) VALUES ('Для кэмбридж классов 2022', '350000', '8', '44', '2', '1');
INSERT INTO `ellipse`.`contract` (`name`, `amount`, `year_id`, `school_id`, `activity_status_id`, `employee_id`) VALUES ('Для русских классов с 1го по 6й 2022', '260000', '8', '44', '2', '1');
INSERT INTO `ellipse`.`contract` (`name`, `amount`, `year_id`, `school_id`, `activity_status_id`, `employee_id`) VALUES ('Для русских классов с 7го по 12й 2022', '310000', '8', '44', '2', '1');

insert into student_contract
SELECT NULL,
       (SELECT
            id
        FROM
            student
        WHERE
            login = t.login),
       8,
       (SELECT
            c.id
        FROM
            contract AS c
        WHERE
            c.amount = t.contract), 0, 1, NOW(), 2, t.contract,0, 0, NOW()
FROM
    students_2022 AS t;


insert into student_orders
SELECT NULL,
       (SELECT
            id
        FROM
            student
        WHERE
            login = t.login),
       5,
       (SELECT
            cn.id
        FROM
            class_name AS cn
                LEFT JOIN
            class_number AS n ON cn.class_number_id = n.id
        WHERE
            CONCAT(n.name, cn.name) = t.class_name),
       (SELECT
            cn.id
        FROM
            class_name AS cn
                LEFT JOIN
            class_number AS n ON cn.class_number_id = n.id
        WHERE
            CONCAT(n.name, cn.name) = t.class_name), 1, 2, 8, NULL, NOW(), 1, 1
FROM
    students_2022 AS t;

insert into student_correction
select NULL, t.contract, NULL, (SELECT
                                    id
                                FROM
                                    student
                                WHERE
                                    login = t.login), 8, 10, NOW(), 1, NOW() from students_2022 as t;


insert into student_orders
SELECT NULL,
       (SELECT
            id
        FROM
            student
        WHERE
            login = t.login),
       1,
       (SELECT
            cn.id
        FROM
            class_name AS cn
                LEFT JOIN
            class_number AS n ON cn.class_number_id = n.id
        WHERE
            CONCAT(n.name, cn.name) = t.class_name),
       (SELECT
            cn.id
        FROM
            class_name AS  cn WHERE
            cn.name  = 'выбывшие 2022'), 2, 4, 8, NULL, NOW(), 1, 1
FROM
    students_2022 AS t where login in ('22032','22029','22033','22045','22007','22012','22039','22040','22026','22031','22038','22006');


drop table if exists students_2023;

-- 1) Создаём таблицу
CREATE TABLE `students_2023` (
                                 `login`       INT UNSIGNED   NOT NULL COMMENT 'Логин ученика',
                                 `class_name`  VARCHAR(10)    NOT NULL COMMENT 'Класс (например 4р,7к и т.п.)',
                                 `surname`     VARCHAR(100)   NOT NULL COMMENT 'Фамилия',
                                 `name`        VARCHAR(100)   NOT NULL COMMENT 'Имя',
                                 `contract`    INT UNSIGNED   NOT NULL COMMENT 'Сумма по контракту',
                                 `gender`      TINYINT        NOT NULL COMMENT '1=мужской, 2=женский',
                                 PRIMARY KEY (`login`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

-- 2) Загружаем данные из CSV
-- Скопируйте файл students_2022.csv в директорию, доступную MySQL (например /var/lib/mysql-files/)
LOAD DATA LOCAL INFILE 'C:\\ProgramData\\MySQL\\MySQL Server 8.0\\Uploads\\students_2023.csv'
INTO TABLE students_2023
CHARACTER SET utf8mb4
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\r\n'
IGNORE 1 LINES
  (`login`,`class_name`,`surname`,`name`,`contract`,`gender`);

UPDATE `ellipse`.`students_2023` SET `login` = 23250 WHERE (`login` = 23045);
UPDATE `ellipse`.`students_2022` SET `login` = 22002 WHERE (`login` = 220002);
UPDATE `ellipse`.`student` SET `login` = '22002' WHERE (`login` = '220002');

UPDATE `ellipse`.`students_2023` SET `class_name` = '0п' WHERE (`login` = '23111');

insert into student_orders
SELECT NULL,
       (SELECT
            id
        FROM
            student
        WHERE
            login = st22.login),
       3,
       (SELECT
            cn.id
        FROM
            class_name AS cn
                LEFT JOIN
            class_number AS n ON cn.class_number_id = n.id
        WHERE
            CONCAT(n.name, cn.name) = st22.class_name),
       (SELECT
            cn.id
        FROM
            class_name AS cn
                LEFT JOIN
            class_number AS n ON cn.class_number_id = n.id
        WHERE
            CONCAT(n.name, cn.name) = st23.class_name), 2, 3, 9, NULL, NOW(), 1, 1
FROM
    students_2022 as st22
        inner join students_2023 as st23 on st22.login = st23.login - 1000;


insert into student
SELECT NULL, t.login, '-', t.name, t.surname, null, NOW(), NULL, 44, t.gender, 9, 1, NOW(), '-'  FROM ellipse.students_2023 as t
where t.login - 1000  not in (select login from students_2022);


insert into student_orders
SELECT NULL,
       (SELECT
            id
        FROM
            student
        WHERE
            login = t.login),
       4,
       200,
       (SELECT
            cn.id
        FROM
            class_name AS cn
                LEFT JOIN
            class_number AS n ON cn.class_number_id = n.id
        WHERE
            CONCAT(n.name, cn.name) = t.class_name), 1, 1, 9, NULL, NOW(), 1, 1
FROM
    students_2023 AS t where t.login - 1000 not in (select login from students_2022);