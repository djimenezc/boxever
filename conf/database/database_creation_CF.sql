create keyspace currencies;

use currencies;

create column family dailyCurrencies with comparator = UTF8Type and
        column_metadata =
        [
        {column_name: id, validation_class: UUIDType},
        {column_name: dateKey, validation_class: DateType},
        {column_name: rate, validation_class: LongType},
        {column_name: currency, validation_class: UTF8Type, index_type: KEYS}
        ];
        
        
create column family dailyCurrencies2 with comparator = UTF8Type and
        column_metadata =
        [
        {column_name: id, validation_class: UUIDType},
        {column_name: dateKey, validation_class: DateType},
        {column_name: rate, validation_class: LongType},
        {column_name: currency, validation_class: UTF8Type, index_type: KEYS}
        ];        