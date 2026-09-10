UPDATE abl.payment_type t
SET t.name = 'Демир Банк'
WHERE t.id = 3;

INSERT INTO abl.payment_type (id, name)
VALUES (2, 'МБанк');

UPDATE abl.acc_cashbox t
SET t.name = 'Демир Банк KGS'
WHERE t.id = 1;

INSERT INTO abl.acc_cashbox (id, name, acc_currency_id, payment_type_id)
VALUES (6, 'МБанк KGS', 1, 2);

