DROP TABLE dailyCurrencies;

CREATE TABLE dailyCurrencies (
  id uuid PRIMARY KEY,
  dateKey bigint,
  currency text,
  rate bigint
 );
 
DROP INDEX date_idx;
CREATE INDEX date_idx
   ON dailyCurrencies (dateKey);
   
DROP INDEX currency_idx;
CREATE INDEX currency_idx
   ON dailyCurrencies (currency); 
   
INSERT INTO dailyCurrencies (id,dateKey, currency, rate)
  VALUES (1,'111111', 'USD','1212');   
INSERT INTO dailyCurrencies (id,dateKey, currency, rate)
  VALUES (2,'111111', 'USD','1213');   
  
SELECT * from dailyCurrencies;



DROP TABLE dailyCurrencies2;

CREATE TABLE dailyCurrencies2 (
  id uuid PRIMARY KEY,
  dateKey bigint,
  USD bigint,
  AUD bigint,
  BGN bigint
 );