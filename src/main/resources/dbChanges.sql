UPDATE indigo.school t
SET t.code           = '11',
    t.primary_code   = '11',
    t.secondary_code = '11'
WHERE t.id = 39;

UPDATE indigo.school t
SET t.code           = '14',
    t.primary_code   = '14',
    t.secondary_code = '14'
WHERE t.id = 38;

UPDATE indigo.school t
SET t.code           = '15',
    t.primary_code   = '15',
    t.secondary_code = '15'
WHERE t.id = 40;

UPDATE indigo.school t
SET t.code           = '16',
    t.primary_code   = '16',
    t.secondary_code = '16'
WHERE t.id = 41;

UPDATE indigo.school t
SET t.code           = '13',
    t.primary_code   = '13',
    t.secondary_code = '13'
WHERE t.id = 36;

UPDATE indigo.school t
SET t.code           = '21',
    t.primary_code   = '21',
    t.secondary_code = '21',
    t.name_en        = 'Indigo Kids School',
    t.name_kg        = '-'
WHERE t.id = 33;

UPDATE indigo.school t
SET t.code           = '22',
    t.primary_code   = '22',
    t.secondary_code = '22'
WHERE t.id = 1;

UPDATE indigo.school t
SET t.code           = '10',
    t.primary_code   = '10',
    t.secondary_code = '10',
    t.name_en        = 'Indigo Office',
    t.name_kg        = '-'
WHERE t.id = 42;

UPDATE indigo.school t
SET t.code           = '23',
    t.primary_code   = '23',
    t.secondary_code = '23'
WHERE t.id = 37;

UPDATE indigo.school t
SET t.code           = '12',
    t.primary_code   = '12',
    t.secondary_code = '12'
WHERE t.id = 35;

UPDATE student
SET student.login = CONCAT((select school.code from school where school.id = student.school_id),
                           SUBSTR(student.login, 3));

update acc_category
set parent_code = '791.02',
    code        = (select school.code from school where school.id = acc_category.school_id)
where school_id is not null
  and employee_id is null
  and parent_id = 37;

update acc_category
set parent_code = '791.01',
    code        = (select school.code from school where school.id = acc_category.school_id)
where school_id is not null
  and employee_id is null
  and parent_id = 34;

update acc_category
set parent_code = '336',
    code        = (select school.code from school where school.id = acc_category.school_id)
where school_id is not null
  and employee_id is null
  and parent_id = 8969;

UPDATE indigo.acc_category t
SET t.name = 'КРАТКОСРОЧНЫЕ ЗАДОЛЖЕННОСТИ - Indigo Kids Club'
WHERE t.id = 10328;

UPDATE indigo.acc_category t
SET t.parent_code = '336.21'
WHERE t.id = 10731;

UPDATE indigo.acc_category t
SET t.parent_code = '336.21'
WHERE t.id = 10676;

UPDATE indigo.acc_category t
SET t.parent_code = '336.22'
WHERE t.id = 10678;

UPDATE indigo.acc_category t
SET t.parent_code = '791.01.22',
    t.parent_id   = 3225
WHERE t.id = 10732;

CREATE TABLE cat AS
SELECT *
FROM acc_category;


update acc_category
set parent_code = (select concat(cat.parent_code, '.', cat.code) from cat where cat.id = acc_category.parent_id)
where school_id is not null
  and employee_id is not null;

drop table if exists cat;