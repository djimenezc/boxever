create keyspace currencies;

CREATE TABLE dailyCurrencies (
  id uuid PRIMARY KEY,
  dateKey timestamp,
  currency text,
  rate bigint
 );
 
 CREATE TABLE dailyCurrencies2 (
  id uuid PRIMARY KEY,
  dateKey timestamp,
  currency text,
  rate bigint
 );
 
CREATE INDEX currency_idx
   ON dailyCurrencies (currency); 

CREATE INDEX currency2_idx
   ON dailyCurrencies2 (currency); 
