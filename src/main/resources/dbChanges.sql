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


update students_2022 as t
set t.class_name =
        (select class_name
         from students_2024 as st24
         where st24.surname = t.surname
           and st24.name = t.name);
update students_2023 as t
set t.class_name =
        (select class_name
         from students_2024 as st24
         where st24.surname = t.surname
           and st24.name = t.name);

update students_2022
set class_name = 'А'
where class_type = 'р'
  and class_name is null;
update students_2022
set class_name = 'Са'
where class_type = 'к'
  and class_name is null;
update students_2022
set class_name = 'под-курс'
where class_type = 'п';

update students_2023
set class_name = 'А'
where class_type = 'р'
  and class_name is null;
update students_2023
set class_name = 'Са'
where class_type = 'к'
  and class_name is null;
update students_2023
set class_name = 'под-курс'
where class_type = 'п';


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

insert into student_relatives
SELECT NULL,
       (SELECT id
        FROM student as st
        WHERE st.surname = t.surname
          and st.name = t.name),
       1,
       '-', NOW(), '-', '-', 1, '-', '-', NULL
FROM students_2022 AS t;

update student
set login = id
where entering_year_id < 11;

INSERT INTO `ellipse`.`class_name` (`name`, `school_id`, `class_number_id`, `activity_status_id`, `class_type_id`)
VALUES ('под-курс', '44', '1', '2', '1');
INSERT INTO `ellipse`.`class_name` (`name`, `school_id`, `class_number_id`, `activity_status_id`, `class_type_id`)
VALUES ('выбывшие 2022', '44', '1', '2', '1');
INSERT INTO `ellipse`.`class_name` (`name`, `school_id`, `class_number_id`, `activity_status_id`, `class_type_id`)
VALUES ('выбывшие 2023', '44', '1', '2', '1');

insert into student_orders
SELECT NULL,
       (SELECT id
        FROM student as st
        WHERE st.surname = t.surname
          and st.name = t.name),
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
VALUES ('Для под-курса 2022', '144000', '8', '44', '2', '1');
INSERT INTO `ellipse`.`contract` (`name`, `amount`, `year_id`, `school_id`, `activity_status_id`, `employee_id`)
VALUES ('Для Кембридж классов 2022', '350000', '8', '44', '2', '1');
INSERT INTO `ellipse`.`contract` (`name`, `amount`, `year_id`, `school_id`, `activity_status_id`, `employee_id`)
VALUES ('Для русских классов с 1го по 6й 2022', '260000', '8', '44', '2', '1');
INSERT INTO `ellipse`.`contract` (`name`, `amount`, `year_id`, `school_id`, `activity_status_id`, `employee_id`)
VALUES ('Для русских классов с 7го по 12й 2022', '310000', '8', '44', '2', '1');

insert into student_contract
SELECT NULL,
       (SELECT id
        FROM student as st
        WHERE st.surname = t.surname
          and st.name = t.name),
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
        WHERE st.surname = t.surname
          and st.name = t.name),
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
        WHERE st.surname = t.surname
          and st.name = t.name),
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
        WHERE st.surname = t.surname
          and st.name = t.name),
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

-- 2023

insert into student_orders
SELECT NULL,
       (SELECT id
        FROM student as st
        WHERE st.surname = st22.surname
          and st.name = st22.name),
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
        WHERE st.surname = st22.surname
          and st.name = st22.name),
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

insert into student_relatives
SELECT NULL,
       (SELECT id
        FROM student as st
        WHERE st.surname = t.surname
          and st.name = t.name),
       1,
       '-', NOW(), '-', '-', 1, '-', '-', NULL
FROM students_2023 AS t
where concat(LOWER(TRIM(t.surname)), LOWER(TRIM(t.name))) not in
      (select concat(LOWER(TRIM(st22.surname)), LOWER(TRIM(st22.name))) from students_2022 as st22);


update student
set login = id
where entering_year_id < 11;


insert into student_orders
SELECT NULL,
       (SELECT id
        FROM student as st
        WHERE st.surname = t.surname
          and st.name = t.name),
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
where concat(LOWER(TRIM(t.surname)), LOWER(TRIM(t.name))) not in
      (select concat(LOWER(TRIM(st22.surname)), LOWER(TRIM(st22.name))) from students_2022 as st22);

UPDATE `ellipse`.`students_2023`
SET `contract` = '380000'
WHERE (`login` = '23075');

INSERT INTO `ellipse`.`contract` (`id`, `name`, `amount`, `year_id`, `school_id`, `activity_status_id`, `employee_id`)
VALUES (NULL, 'Для Кембридж классов 2022', '350000.00', '9', '44', '2', '1');
INSERT INTO `ellipse`.`contract` (`id`, `name`, `amount`, `year_id`, `school_id`, `activity_status_id`, `employee_id`)
VALUES (NULL, 'Для русских классов с 1го по 6й 2022', '260000.00', '9', '44', '2', '1');
INSERT INTO `ellipse`.`contract` (`id`, `name`, `amount`, `year_id`, `school_id`, `activity_status_id`, `employee_id`)
VALUES (NULL, 'Для русских классов с 7го по 12й 2022', '310000.00', '9', '44', '2', '1');
INSERT INTO `ellipse`.`contract` (`id`, `name`, `amount`, `year_id`, `school_id`, `activity_status_id`, `employee_id`)
VALUES (NULL, 'Для под-курса 2023', '162000', '9', '44', '2', '1');
INSERT INTO `ellipse`.`contract` (`id`, `name`, `amount`, `year_id`, `school_id`, `activity_status_id`, `employee_id`)
VALUES (NULL, 'Для Кембридж классов 2023', '380000', '9', '44', '2', '1');
INSERT INTO `ellipse`.`contract` (`id`, `name`, `amount`, `year_id`, `school_id`, `activity_status_id`, `employee_id`)
VALUES (NULL, 'Для русских классов с 1го по 6й 2023', '280000', '9', '44', '2', '1');
INSERT INTO `ellipse`.`contract` (`id`, `name`, `amount`, `year_id`, `school_id`, `activity_status_id`, `employee_id`)
VALUES (NULL, 'Для русских классов с 7го по 12й 2023', '330000.00', '9', '44', '2', '1');


insert into student_contract
SELECT NULL,
       (SELECT id
        FROM student as st
        WHERE st.surname = t.surname
          and st.name = t.name),
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
where concat(LOWER(TRIM(t.surname)), LOWER(TRIM(t.name))) not in
      (select concat(LOWER(TRIM(st22.surname)), LOWER(TRIM(st22.name))) from students_2022 as st22);


insert into student_contract
SELECT NULL,
       (SELECT id
        FROM student as st
        WHERE st.surname = t.surname
          and st.name = t.name),
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
where concat(LOWER(TRIM(t.surname)), LOWER(TRIM(t.name))) in
      (select concat(LOWER(TRIM(st22.surname)), LOWER(TRIM(st22.name))) from students_2022 as st22);


insert into student_orders
SELECT NULL,
       (SELECT id
        FROM student as st
        WHERE st.surname = t.surname
          and st.name = t.name),
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
where concat(LOWER(TRIM(t.surname)), LOWER(TRIM(t.name))) not in
      (select concat(LOWER(TRIM(st22.surname)), LOWER(TRIM(st22.name))) from students_2022 as st22);


insert into student_orders
SELECT NULL,
       (SELECT id
        FROM student as st
        WHERE st.surname = t.surname
          and st.name = t.name),
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
where concat(LOWER(TRIM(t.surname)), LOWER(TRIM(t.name))) in
      (select concat(LOWER(TRIM(st22.surname)), LOWER(TRIM(st22.name))) from students_2022 as st22);


insert into student_correction
select NULL,
       t.contract,
       NULL,
       (SELECT id
        FROM student as st
        WHERE st.surname = t.surname
          and st.name = t.name),
       9,
       10,
       NOW(),
       1,
       NOW()
from students_2023 as t
where concat(LOWER(TRIM(t.surname)), LOWER(TRIM(t.name))) not in
      (select concat(LOWER(TRIM(st22.surname)), LOWER(TRIM(st22.name))) from students_2022 as st22);


insert into student_correction
select NULL,
       t.contract,
       NULL,
       (SELECT id
        FROM student as st
        WHERE st.surname = t.surname
          and st.name = t.name),
       9,
       10,
       NOW(),
       1,
       NOW()
from students_2023 as t
where concat(LOWER(TRIM(t.surname)), LOWER(TRIM(t.name))) in
      (select concat(LOWER(TRIM(st22.surname)), LOWER(TRIM(st22.name))) from students_2022 as st22);


insert into student_orders
SELECT NULL,
       (SELECT id
        FROM student as st
        WHERE st.surname = t.surname
          and st.name = t.name),
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
  and concat(LOWER(TRIM(t.surname)), LOWER(TRIM(t.name))) not in
      (select concat(LOWER(TRIM(st22.surname)), LOWER(TRIM(st22.name))) from students_2022 as st22);


insert into student_orders
SELECT NULL,
       (SELECT id
        FROM student as st
        WHERE st.surname = t.surname
          and st.name = t.name),
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
  and concat(LOWER(TRIM(t.surname)), LOWER(TRIM(t.name))) in
      (select concat(LOWER(TRIM(st22.surname)), LOWER(TRIM(st22.name))) from students_2022 as st22);

-- 2024


INSERT INTO `ellipse`.`contract` (`id`, `name`, `amount`, `year_id`, `school_id`, `activity_status_id`, `employee_id`)
VALUES (NULL, 'Для Кембридж классов 2022', '350000.00', '10', '44', '2', '1');
INSERT INTO `ellipse`.`contract` (`id`, `name`, `amount`, `year_id`, `school_id`, `activity_status_id`, `employee_id`)
VALUES (NULL, 'Для русских классов с 1го по 6й 2022', '260000.00', '10', '44', '2', '1');
INSERT INTO `ellipse`.`contract` (`id`, `name`, `amount`, `year_id`, `school_id`, `activity_status_id`, `employee_id`)
VALUES (NULL, 'Для русских классов с 7го по 12й 2022', '310000.00', '10', '44', '2', '1');
INSERT INTO `ellipse`.`contract` (`id`, `name`, `amount`, `year_id`, `school_id`, `activity_status_id`, `employee_id`)
VALUES (NULL, 'Для Кембридж классов 2024', '415000', '10', '44', '2', '1');
INSERT INTO `ellipse`.`contract` (`id`, `name`, `amount`, `year_id`, `school_id`, `activity_status_id`, `employee_id`)
VALUES (NULL, 'Для русских классов с 1го по 6й 2024', '305000', '10', '44', '2', '1');
INSERT INTO `ellipse`.`contract` (`id`, `name`, `amount`, `year_id`, `school_id`, `activity_status_id`, `employee_id`)
VALUES (NULL, 'Для русских классов с 7го по 12й 2024', '360000', '10', '44', '2', '1');



insert into student_orders
SELECT NULL,
       (SELECT id
        FROM student as st
        WHERE st.surname = st23.surname
          and st.name = st23.name),
       4,
       (SELECT cn.id
        FROM class_name AS cn
                 LEFT JOIN
             class_number AS n ON cn.class_number_id = n.id
        WHERE CONCAT(n.name, cn.name) = concat(st23.class_number, st23.class_name)),
       (SELECT cn.id
        FROM class_name AS cn
                 LEFT JOIN
             class_number AS n ON cn.class_number_id = n.id
        WHERE CONCAT(n.name, cn.name) = concat(st23.class_number, st23.class_name)),
       2,
       1,
       10,
       NULL,
       NOW(),
       1,
       1
FROM students_2023 as st23
         inner join students_2024 as st24 ON LOWER(TRIM(st23.surname)) = LOWER(TRIM(st24.surname))
    AND LOWER(TRIM(st23.name)) = LOWER(TRIM(st24.name));

insert into student_orders
SELECT NULL,
       (SELECT id
        FROM student as st
        WHERE st.surname = st23.surname
          and st.name = st23.name),
       3,
       (SELECT cn.id
        FROM class_name AS cn
                 LEFT JOIN
             class_number AS n ON cn.class_number_id = n.id
        WHERE CONCAT(n.name, cn.name) = concat(st23.class_number, st23.class_name)),
       (SELECT cn.id
        FROM class_name AS cn
                 LEFT JOIN
             class_number AS n ON cn.class_number_id = n.id
        WHERE CONCAT(n.name, cn.name) = concat(st24.class_number, st24.class_name)),
       1,
       3,
       10,
       NULL,
       NOW(),
       1,
       1
FROM students_2023 as st23
         inner join students_2024 as st24 ON LOWER(TRIM(st23.surname)) = LOWER(TRIM(st24.surname))
    AND LOWER(TRIM(st23.name)) = LOWER(TRIM(st24.name));


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
       10,
       1,
       NOW(),
       '-'
FROM ellipse.students_2024 as t
where concat(LOWER(TRIM(t.surname)), LOWER(TRIM(t.name))) not in
      (select concat(LOWER(TRIM(st23.surname)), LOWER(TRIM(st23.name))) from students_2023 as st23);


insert into student_relatives
SELECT NULL,
       (SELECT id
        FROM student as st
        WHERE st.surname = t.surname
          and st.name = t.name),
       1,
       '-', NOW(), '-', '-', 1, '-', '-', NULL
FROM students_2024 AS t
where concat(LOWER(TRIM(t.surname)), LOWER(TRIM(t.name))) not in
      (select concat(LOWER(TRIM(st23.surname)), LOWER(TRIM(st23.name))) from students_2023 as st23);


update student
set login = id
where entering_year_id < 11;

UPDATE `ellipse`.`student`
SET `entering_year_id` = '10'
WHERE (`id` = '59599');

insert into student_orders
VALUES (NULL, 59599,
        4,
        200,
        1500,
        1,
        1,
        10,
        NULL,
        NOW(),
        1,
        1);

insert into student_contract
VALUES (NULL, 59599,
       10, (SELECT c.id
        FROM contract AS c
        WHERE c.amount = 305000.0
          and year_id = 10),
       0,
       1,
       NOW(),
       2,
       305000.0,
       0,
       0,
       NOW());

insert into student_orders
VALUES (NULL,
        59599,
       5,1500,1500,
       1,
       2,
       10,
       NULL,
       NOW(),
       1,
       1);

insert into student_correction
VALUES (NULL,
       305000.0,
       NULL, 59599,
       10,
       10,
       NOW(),
       1,
       NOW());
UPDATE `ellipse`.`student_orders` SET `from_class_name_id` = '1500' WHERE (`id` = '949796');

insert into student_orders
SELECT NULL,
       (SELECT id
        FROM student as st
        WHERE st.surname = t.surname
          and st.name = t.name),
       4,
       200,
       (SELECT cn.id
        FROM class_name AS cn
                 LEFT JOIN
             class_number AS n ON cn.class_number_id = n.id
        WHERE CONCAT(n.name, cn.name) = concat(t.class_number, t.class_name)),
       1,
       1,
       10,
       NULL,
       NOW(),
       1,
       1
FROM students_2024 AS t
where concat(LOWER(TRIM(t.surname)), LOWER(TRIM(t.name))) not in
      (select concat(LOWER(TRIM(st23.surname)), LOWER(TRIM(st23.name))) from students_2023 as st23);


insert into student_contract
SELECT NULL,
       (SELECT id
        FROM student as st
        WHERE st.surname = t.surname
          and st.name = t.name),
       10,
       (SELECT c.id
        FROM contract AS c
        WHERE c.amount = t.contract
          and year_id = 10),
       0,
       1,
       NOW(),
       2,
       t.contract,
       0,
       0,
       NOW()
FROM students_2024 AS t
where concat(LOWER(TRIM(t.surname)), LOWER(TRIM(t.name))) not in
      (select concat(LOWER(TRIM(st23.surname)), LOWER(TRIM(st23.name))) from students_2023 as st23);


insert into student_contract
SELECT NULL,
       (SELECT id
        FROM student as st
        WHERE st.surname = t.surname
          and st.name = t.name),
       10,
       (SELECT c.id
        FROM contract AS c
        WHERE c.amount = t.contract
          and year_id = 10),
       0,
       1,
       NOW(),
       2,
       t.contract,
       0,
       0,
       NOW()
FROM students_2024 AS t
where concat(LOWER(TRIM(t.surname)), LOWER(TRIM(t.name))) in
      (select concat(LOWER(TRIM(st23.surname)), LOWER(TRIM(st23.name))) from students_2023 as st23);


insert into student_orders
SELECT NULL,
       (SELECT id
        FROM student as st
        WHERE st.surname = t.surname
          and st.name = t.name),
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
       10,
       NULL,
       NOW(),
       1,
       1
FROM students_2024 AS t
where concat(LOWER(TRIM(t.surname)), LOWER(TRIM(t.name))) not in
      (select concat(LOWER(TRIM(st23.surname)), LOWER(TRIM(st23.name))) from students_2023 as st23);


insert into student_orders
SELECT NULL,
       (SELECT id
        FROM student as st
        WHERE st.surname = t.surname
          and st.name = t.name),
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
       10,
       NULL,
       NOW(),
       1,
       1
FROM students_2024 AS t
where concat(LOWER(TRIM(t.surname)), LOWER(TRIM(t.name))) in
      (select concat(LOWER(TRIM(st23.surname)), LOWER(TRIM(st23.name))) from students_2023 as st23);


insert into student_correction
select NULL,
       t.contract,
       NULL,
       (SELECT id
        FROM student as st
        WHERE st.surname = t.surname
          and st.name = t.name),
       10,
       10,
       NOW(),
       1,
       NOW()
from students_2024 as t
where concat(LOWER(TRIM(t.surname)), LOWER(TRIM(t.name))) not in
      (select concat(LOWER(TRIM(st23.surname)), LOWER(TRIM(st23.name))) from students_2023 as st23);


insert into student_correction
select NULL,
       t.contract,
       NULL,
       (SELECT id
        FROM student as st
        WHERE st.surname = t.surname
          and st.name = t.name),
       10,
       10,
       NOW(),
       1,
       NOW()
from students_2024 as t
where concat(LOWER(TRIM(t.surname)), LOWER(TRIM(t.name))) in
      (select concat(LOWER(TRIM(st23.surname)), LOWER(TRIM(st23.name))) from students_2023 as st23);
