use
ellipse;

drop table if exists students_2022;

CREATE TABLE `students_2022`
(
    `login`        INT UNSIGNED   NOT NULL COMMENT 'Логин ученика',
    `class_number` VARCHAR(10),
    `class_type`   VARCHAR(10),
    `class_name`   VARCHAR(10),
    `surname`      VARCHAR(100) NOT NULL COMMENT 'Фамилия',
    `name`         VARCHAR(100) NOT NULL COMMENT 'Имя',
    `contract`     INT UNSIGNED   NOT NULL COMMENT 'Сумма по контракту',
    `gender`       TINYINT      NOT NULL COMMENT '1=мужской, 2=женский',
    PRIMARY KEY (`login`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

LOAD
DATA LOCAL INFILE 'C:\\ProgramData\\MySQL\\MySQL Server 8.0\\Uploads\\students_2022.csv'
INTO TABLE students_2022
CHARACTER SET utf8mb4
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\r\n'
IGNORE 1 LINES
  (`login`,`class_number`,`class_type`,`class_name`,`surname`,`name`,`contract`,`gender`);


drop table if exists students_2023;

CREATE TABLE `students_2023`
(
    `login`        INT UNSIGNED   NOT NULL COMMENT 'Логин ученика',
    `class_number` VARCHAR(10),
    `class_type`   VARCHAR(10),
    `class_name`   VARCHAR(10),
    `surname`      VARCHAR(100) NOT NULL COMMENT 'Фамилия',
    `name`         VARCHAR(100) NOT NULL COMMENT 'Имя',
    `contract`     INT UNSIGNED   NOT NULL COMMENT 'Сумма по контракту',
    `gender`       TINYINT      NOT NULL COMMENT '1=мужской, 2=женский',
    PRIMARY KEY (`login`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

LOAD
DATA LOCAL INFILE 'C:\\ProgramData\\MySQL\\MySQL Server 8.0\\Uploads\\students_2023.csv'
INTO TABLE students_2023
CHARACTER SET utf8mb4
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\r\n'
IGNORE 1 LINES
  (`login`,`class_number`,`class_type`,`class_name`,`surname`,`name`,`contract`,`gender`);


drop table if exists students_2024;

CREATE TABLE `students_2024`
(
    `login`        INT UNSIGNED   NOT NULL COMMENT 'Логин ученика',
    `class_number` VARCHAR(10),
    `class_type`   VARCHAR(10),
    `class_name`   VARCHAR(10),
    `surname`      VARCHAR(100) NOT NULL COMMENT 'Фамилия',
    `name`         VARCHAR(100) NOT NULL COMMENT 'Имя',
    `contract`     INT UNSIGNED   NOT NULL COMMENT 'Сумма по контракту',
    `gender`       TINYINT      NOT NULL COMMENT '1=мужской, 2=женский',
    `year`         INT          NOT NULL,
    PRIMARY KEY (`login`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

LOAD
DATA LOCAL INFILE 'C:\\ProgramData\\MySQL\\MySQL Server 8.0\\Uploads\\students_2024.csv'
INTO TABLE students_2024
CHARACTER SET utf8mb4
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\r\n'
IGNORE 1 LINES
  (`login`,`class_number`,`class_type`,`class_name`,`surname`,`name`,`contract`,`gender`,`year`);


update students_2022 as t set t.class_name =
                                  (select class_name from students_2024 as st24
                                                     where st24.surname = t.surname and st24.name = t.name);
update students_2023 as t set t.class_name =
                                  (select class_name from students_2024 as st24
                                   where st24.surname = t.surname and st24.name = t.name);

update students_2022
set class_name = 'А' where class_type = 'р' and class_name is null;
update students_2022
set class_name = 'Са' where class_type = 'к' and class_name is null;
update students_2022
set class_name = 'подкурс' where class_type = 'п';

update students_2023
set class_name = 'А' where class_type = 'р' and class_name is null;
update students_2023
set class_name = 'Са' where class_type = 'к' and class_name is null;
update students_2023
set class_name = 'подкурс' where class_type = 'п';


insert into student
SELECT NULL,
       t.login,
       '-',
       t.name,
       t.surname,
       null,
       NOW(),
       NULL,
       44,
       t.gender,
       8,
       1,
       NOW(),
       '-'
FROM ellipse.students_2022 as t;

update student set login = id;

INSERT INTO `ellipse`.`class_name` (`name`, `school_id`, `class_number_id`, `activity_status_id`, `class_type_id`)
VALUES ('подкурс', '44', '1', '2', '1');
INSERT INTO `ellipse`.`class_name` (`name`, `school_id`, `class_number_id`, `activity_status_id`, `class_type_id`)
VALUES ('выбывшие 2022', '44', '1', '2', '1');
INSERT INTO `ellipse`.`class_name` (`name`, `school_id`, `class_number_id`, `activity_status_id`, `class_type_id`)
VALUES ('выбывшие 2023', '44', '1', '2', '1');

insert into student_orders
SELECT NULL,
       (SELECT id
        FROM student as st
        WHERE st.surname = t.surname and st.name = t.name),
       4,
       200,
       (SELECT cn.id
        FROM class_name AS cn
                 LEFT JOIN
             class_number AS n ON cn.class_number_id = n.id
        WHERE CONCAT(n.name, cn.name) = concat(t.class_number, t.class_name)),
       1,
       1,
       8,
       NULL,
       NOW(),
       1,
       1
FROM students_2022 AS t;

INSERT INTO `ellipse`.`contract` (`name`, `amount`, `year_id`, `school_id`, `activity_status_id`, `employee_id`)
VALUES ('Для подкурса 2022', '144000', '8', '44', '2', '1');
INSERT INTO `ellipse`.`contract` (`name`, `amount`, `year_id`, `school_id`, `activity_status_id`, `employee_id`)
VALUES ('Для Кэмбридж классов 2022', '350000', '8', '44', '2', '1');
INSERT INTO `ellipse`.`contract` (`name`, `amount`, `year_id`, `school_id`, `activity_status_id`, `employee_id`)
VALUES ('Для русских классов с 1го по 6й 2022', '260000', '8', '44', '2', '1');
INSERT INTO `ellipse`.`contract` (`name`, `amount`, `year_id`, `school_id`, `activity_status_id`, `employee_id`)
VALUES ('Для русских классов с 7го по 12й 2022', '310000', '8', '44', '2', '1');

insert into student_contract
SELECT NULL,
       (SELECT id
        FROM student as st
        WHERE st.surname = t.surname and st.name = t.name),
       8,
       (SELECT c.id
        FROM contract AS c
        WHERE c.amount = t.contract
          and year_id = 8),
       0,
       1,
       NOW(),
       2,
       t.contract,
       0,
       0,
       NOW()
FROM students_2022 AS t;


insert into student_orders
SELECT NULL,
       (SELECT id
        FROM student as st
        WHERE st.surname = t.surname and st.name = t.name),
       5,
       (SELECT cn.id
        FROM class_name AS cn
                 LEFT JOIN
             class_number AS n ON cn.class_number_id = n.id
        WHERE CONCAT(n.name, cn.name) = concat(t.class_number, t.class_name)),
       (SELECT cn.id
        FROM class_name AS cn
                 LEFT JOIN
             class_number AS n ON cn.class_number_id = n.id
        WHERE CONCAT(n.name, cn.name) = concat(t.class_number, t.class_name)),
       1,
       2,
       8,
       NULL,
       NOW(),
       1,
       1
FROM students_2022 AS t;

insert into student_correction
select NULL,
       t.contract,
       NULL,
       (SELECT id
        FROM student as st
        WHERE st.surname = t.surname and st.name = t.name),
       8,
       10,
       NOW(),
       1,
       NOW()
from students_2022 as t;


insert into student_orders
SELECT NULL,
       (SELECT id
        FROM student as st
        WHERE st.surname = t.surname and st.name = t.name),
       1,
       (SELECT cn.id
        FROM class_name AS cn
                 LEFT JOIN
             class_number AS n ON cn.class_number_id = n.id
        WHERE CONCAT(n.name, cn.name) = concat(t.class_number, t.class_name)),
       (SELECT cn.id
        FROM class_name AS cn
        WHERE cn.name = 'выбывшие 2022'),
       2,
       4,
       8,
       NULL,
       NOW(),
       1,
       1
FROM students_2022 AS t
where login in
      ('22032', '22029', '22033', '22045', '22007', '22012', '22039', '22040', '22026', '22031', '22038', '22006');


insert into student_orders
SELECT NULL,
       (SELECT id
        FROM student as st
        WHERE st.surname = st22.surname and st.name = st22.name),
       4,
       (SELECT cn.id
        FROM class_name AS cn
                 LEFT JOIN
             class_number AS n ON cn.class_number_id = n.id
        WHERE CONCAT(n.name, cn.name) = concat(st22.class_number, st22.class_name)),
       (SELECT cn.id
        FROM class_name AS cn
                 LEFT JOIN
             class_number AS n ON cn.class_number_id = n.id
        WHERE CONCAT(n.name, cn.name) = concat(st22.class_number, st22.class_name)),
       2,
       1,
       9,
       NULL,
       NOW(),
       1,
       1
FROM students_2022 as st22
         inner join students_2023 as st23 ON LOWER(TRIM(st22.surname)) = LOWER(TRIM(st23.surname))
    AND LOWER(TRIM(st22.name)) = LOWER(TRIM(st23.name));

insert into student_orders
SELECT NULL,
       (SELECT id
        FROM student as st
        WHERE st.surname = st22.surname and st.name = st22.name),
       3,
       (SELECT cn.id
        FROM class_name AS cn
                 LEFT JOIN
             class_number AS n ON cn.class_number_id = n.id
        WHERE CONCAT(n.name, cn.name) = concat(st22.class_number, st22.class_name)),
       (SELECT cn.id
        FROM class_name AS cn
                 LEFT JOIN
             class_number AS n ON cn.class_number_id = n.id
        WHERE CONCAT(n.name, cn.name) = concat(st23.class_number, st23.class_name)),
       1,
       3,
       9,
       NULL,
       NOW(),
       1,
       1
FROM students_2022 as st22
         inner join students_2023 as st23 ON LOWER(TRIM(st22.surname)) = LOWER(TRIM(st23.surname))
    AND LOWER(TRIM(st22.name)) = LOWER(TRIM(st23.name));


insert into student
SELECT NULL,
       t.login,
       '-',
       t.name,
       t.surname,
       null,
       NOW(),
       NULL,
       44,
       t.gender,
       9,
       1,
       NOW(),
       '-'
FROM ellipse.students_2023 as t
where concat(LOWER(TRIM(t.surname)), LOWER(TRIM(t.name))) not in
      (select concat(LOWER(TRIM(st22.surname)), LOWER(TRIM(st22.name))) from students_2022 as st22);

update student set login = id;


insert into student_orders
SELECT NULL,
       (SELECT id
        FROM student as st
        WHERE st.surname = t.surname and st.name = t.name),
       4,
       200,
       (SELECT cn.id
        FROM class_name AS cn
                 LEFT JOIN
             class_number AS n ON cn.class_number_id = n.id
        WHERE CONCAT(n.name, cn.name) = concat(t.class_number, t.class_name)),
       1,
       1,
       9,
       NULL,
       NOW(),
       1,
       1
FROM students_2023 AS t
where concat(LOWER(TRIM(t.surname)), LOWER(TRIM(t.name))) not in (select concat(LOWER(TRIM(st22.surname)), LOWER(TRIM(st22.name))) from students_2022 as st22);

UPDATE `ellipse`.`students_2023`
SET `contract` = '380000'
WHERE (`login` = '23075');

INSERT INTO `ellipse`.`contract` (`id`, `name`, `amount`, `year_id`, `school_id`, `activity_status_id`, `employee_id`)
VALUES (NULL, 'Для Кэмбридж классов 2022', '350000.00', '9', '44', '2', '1');
INSERT INTO `ellipse`.`contract` (`id`, `name`, `amount`, `year_id`, `school_id`, `activity_status_id`, `employee_id`)
VALUES (NULL, 'Для русских классов с 1го по 6й 2022', '260000.00', '9', '44', '2', '1');
INSERT INTO `ellipse`.`contract` (`id`, `name`, `amount`, `year_id`, `school_id`, `activity_status_id`, `employee_id`)
VALUES (NULL, 'Для русских классов с 7го по 12й 2022', '310000.00', '9', '44', '2', '1');
INSERT INTO `ellipse`.`contract` (`id`, `name`, `amount`, `year_id`, `school_id`, `activity_status_id`, `employee_id`)
VALUES (NULL, 'Для подкурса 2023', '162000', '9', '44', '2', '1');
INSERT INTO `ellipse`.`contract` (`id`, `name`, `amount`, `year_id`, `school_id`, `activity_status_id`, `employee_id`)
VALUES (NULL, 'Для Кэмбридж классов 2023', '380000', '9', '44', '2', '1');
INSERT INTO `ellipse`.`contract` (`id`, `name`, `amount`, `year_id`, `school_id`, `activity_status_id`, `employee_id`)
VALUES (NULL, 'Для русских классов с 1го по 6й 2023', '280000', '9', '44', '2', '1');
INSERT INTO `ellipse`.`contract` (`id`, `name`, `amount`, `year_id`, `school_id`, `activity_status_id`, `employee_id`)
VALUES (NULL, 'Для русских классов с 7го по 12й 2023', '330000.00', '9', '44', '2', '1');


insert into student_contract
SELECT NULL,
       (SELECT id
        FROM student as st
        WHERE st.surname = t.surname and st.name = t.name),
       9,
       (SELECT c.id
        FROM contract AS c
        WHERE c.amount = t.contract
          and year_id = 9),
       0,
       1,
       NOW(),
       2,
       t.contract,
       0,
       0,
       NOW()
FROM students_2023 AS t
where concat(LOWER(TRIM(t.surname)), LOWER(TRIM(t.name))) not in (select concat(LOWER(TRIM(st22.surname)), LOWER(TRIM(st22.name))) from students_2022 as st22);


insert into student_contract
SELECT NULL,
       (SELECT id
        FROM student as st
        WHERE st.surname = t.surname and st.name = t.name),
       9,
       (SELECT c.id
        FROM contract AS c
        WHERE c.amount = t.contract
          and year_id = 9),
       0,
       1,
       NOW(),
       2,
       t.contract,
       0,
       0,
       NOW()
FROM students_2023 AS t
where concat(LOWER(TRIM(t.surname)), LOWER(TRIM(t.name))) in (select concat(LOWER(TRIM(st22.surname)), LOWER(TRIM(st22.name))) from students_2022 as st22);


insert into student_orders
SELECT NULL,
       (SELECT id
        FROM student as st
        WHERE st.surname = t.surname and st.name = t.name),
       5,
       (SELECT cn.id
        FROM class_name AS cn
                 LEFT JOIN
             class_number AS n ON cn.class_number_id = n.id
        WHERE CONCAT(n.name, cn.name) = concat(t.class_number, t.class_name)),
       (SELECT cn.id
        FROM class_name AS cn
                 LEFT JOIN
             class_number AS n ON cn.class_number_id = n.id
        WHERE CONCAT(n.name, cn.name) = concat(t.class_number, t.class_name)),
       1,
       2,
       9,
       NULL,
       NOW(),
       1,
       1
FROM students_2023 AS t
where concat(LOWER(TRIM(t.surname)), LOWER(TRIM(t.name))) not in (select concat(LOWER(TRIM(st22.surname)), LOWER(TRIM(st22.name))) from students_2022 as st22);


insert into student_orders
SELECT NULL,
       (SELECT id
        FROM student as st
        WHERE st.surname = t.surname and st.name = t.name),
       5,
       (SELECT cn.id
        FROM class_name AS cn
                 LEFT JOIN
             class_number AS n ON cn.class_number_id = n.id
        WHERE CONCAT(n.name, cn.name) = concat(t.class_number, t.class_name)),
       (SELECT cn.id
        FROM class_name AS cn
                 LEFT JOIN
             class_number AS n ON cn.class_number_id = n.id
        WHERE CONCAT(n.name, cn.name) = concat(t.class_number, t.class_name)),
       3,
       2,
       9,
       NULL,
       NOW(),
       1,
       1
FROM students_2023 AS t
where concat(LOWER(TRIM(t.surname)), LOWER(TRIM(t.name))) in (select concat(LOWER(TRIM(st22.surname)), LOWER(TRIM(st22.name))) from students_2022 as st22);


insert into student_correction
select NULL,
       t.contract,
       NULL,
       (SELECT id
        FROM student as st
        WHERE st.surname = t.surname and st.name = t.name),
       9,
       10,
       NOW(),
       1,
       NOW()
from students_2023 as t
where concat(LOWER(TRIM(t.surname)), LOWER(TRIM(t.name))) not in (select concat(LOWER(TRIM(st22.surname)), LOWER(TRIM(st22.name))) from students_2022 as st22);


insert into student_correction
select NULL,
       t.contract,
       NULL,
       (SELECT id
        FROM student as st
        WHERE st.surname = t.surname and st.name = t.name),
       9,
       10,
       NOW(),
       1,
       NOW()
from students_2023 as t
where concat(LOWER(TRIM(t.surname)), LOWER(TRIM(t.name))) in (select concat(LOWER(TRIM(st22.surname)), LOWER(TRIM(st22.name))) from students_2022 as st22);


insert into student_orders
SELECT NULL,
       (SELECT id
        FROM student as st
        WHERE st.surname = t.surname and st.name = t.name),
       1,
       (SELECT cn.id
        FROM class_name AS cn
                 LEFT JOIN
             class_number AS n ON cn.class_number_id = n.id
        WHERE CONCAT(n.name, cn.name) = concat(t.class_number, t.class_name)),
       (SELECT cn.id
        FROM class_name AS cn
        WHERE cn.name = 'выбывшие 2023'),
       2,
       4,
       9,
       NULL,
       NOW(),
       1,
       1
FROM students_2023 AS t
where login in
      ('23191', '23168', '23169', '23171', '23025', '23186', '23184', '23185', '23176', '23100', '23194', '23170',
       '23111', '23090', '23159', '23182')
  and concat(LOWER(TRIM(t.surname)), LOWER(TRIM(t.name))) not in (select concat(LOWER(TRIM(st22.surname)), LOWER(TRIM(st22.name))) from students_2022 as st22);


insert into student_orders
SELECT NULL,
       (SELECT id
        FROM student as st
        WHERE st.surname = t.surname and st.name = t.name),
       1,
       (SELECT cn.id
        FROM class_name AS cn
                 LEFT JOIN
             class_number AS n ON cn.class_number_id = n.id
        WHERE CONCAT(n.name, cn.name) = concat(t.class_number, t.class_name)),
       (SELECT cn.id
        FROM class_name AS cn
        WHERE cn.name = 'выбывшие 2023'),
       2,
       4,
       9,
       NULL,
       NOW(),
       1,
       1
FROM students_2023 AS t
where login in
      ('23191', '23168', '23169', '23171', '23025', '23186', '23184', '23185', '23176', '23100', '23194', '23170',
       '23111', '23090', '23159', '23182')
  and concat(LOWER(TRIM(t.surname)), LOWER(TRIM(t.name))) in (select concat(LOWER(TRIM(st22.surname)), LOWER(TRIM(st22.name))) from students_2022 as st22);