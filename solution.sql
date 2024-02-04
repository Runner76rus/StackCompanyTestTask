-- Solution 1:
-- Получает номера услуг строкой и дату
-- и возвращает количество показаний по услуге для каждого лицевого
CREATE OR REPLACE FUNCTION stack.select_count_pok_by_service(num_service VARCHAR(255), in_date DATE)
    RETURNS TABLE
            (
                acc   INT,
                serv  INT,
                count INT
            )
AS
$$
SELECT Accounts.number, Counters.service, COUNT(Meter_pok.value)
FROM stack.Accounts
         INNER JOIN stack.Counters ON Accounts.row_id = Counters.acc_id
         INNER JOIN stack.Meter_pok ON Counters.row_id = Meter_pok.counter_id
WHERE Counters.service = num_service::integer
  AND Meter_pok.month = in_date
GROUP BY Accounts.number, Counters.service;
$$ LANGUAGE 'sql';


-- Solution2:
-- Получает номер дома и месяц и возвращает все лицевые в этом доме ,
-- для лицевых выводятся все счетчики с сумарным расходом за месяц
-- ( суммирую все показания тарифов)
CREATE OR REPLACE FUNCTION stack.select_value_by_house_and_month(num_house INT, tar_month DATE)
    RETURNS TABLE
            (
                acc   INT,
                name  text,
                value BIGINT
            )
AS
$$
DECLARE
    id_house INT;
BEGIN
    SELECT Accounts.row_id
    FROM stack.Accounts
    WHERE Accounts.number = num_house
    INTO id_house;

    RETURN QUERY
        SELECT acc1.number,
               Counters.name,
               SUM(Meter_pok.value)
        FROM stack.Meter_pok
                 INNER JOIN stack.Counters ON Meter_pok.counter_id = Counters.row_id
                 INNER JOIN stack.Accounts AS acc1 ON Counters.acc_id = acc1.row_id
                 INNER JOIN stack.Accounts AS acc2 ON acc1.parent_id = acc2.row_id
        WHERE acc1.type = 3
            AND acc2.parent_id = id_house
           OR acc1.parent_id = id_house
        GROUP BY acc1.number, Counters.name;
END;
$$ LANGUAGE plpgsql;

-- Solution 3:
-- Получает номер лицевого
-- и возвращает дату,тариф,объем последнего показания по каждой услуге
CREATE OR REPLACE FUNCTION stack.select_last_pok_by_acc(num_acc INT)
    RETURNS TABLE
            (
                acc   INT,
                serv  INT,
                date  DATE,
                tarif INT,
                value INT
            )
AS
$$
BEGIN
    RETURN QUERY
        SELECT acc_in, serv_in, date_in, tarif_in, value_in
        FROM (SELECT Accounts.number  AS acc_in,
                     Counters.service AS serv_in,
                     Meter_pok.date   AS date_in,
                     Meter_pok.tarif  AS tarif_in,
                     Meter_pok.value  AS value_in,
                     ROW_NUMBER()
                     OVER (
                         PARTITION BY Counters.service, Meter_pok.tarif
                         ORDER BY Meter_pok.date DESC
                         )            AS rn
              FROM stack.Accounts
                       INNER JOIN stack.Counters ON Accounts.row_id = Counters.acc_id
                       INNER JOIN stack.Meter_pok ON Counters.row_id = Meter_pok.counter_id
              WHERE Accounts.number = num_acc) AS subquery
        WHERE rn = 1;
END;
$$ LANGUAGE plpgsql;

