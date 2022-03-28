# Manipulate a Stream

## Describe a stream

```sql
describe userprofile;

Name                 : USERPROFILE
 Field       | Type
-------------------------------
 USERID      | INTEGER
 FIRSTNAME   | VARCHAR(STRING)
 LASTNAME    | VARCHAR(STRING)
 COUNTRYCODE | VARCHAR(STRING)
 RATING      | DOUBLE
-------------------------------
For runtime statistics and query details run: DESCRIBE <Stream,Table> EXTENDED;
```

We also have in every stream two additional fields `ROWTIME` and `ROWKEY`. The first one represents the field time of the data record and the second one represents the key of the topic.
```
ksql> select rowtime, firstname, lastname from userprofile emit changes limit 5;
+-----------------------+-----------------------+-----------------------+
|ROWTIME                |FIRSTNAME              |LASTNAME               |
+-----------------------+-----------------------+-----------------------+
|1648490100889          |Ivan                   |Fawcett                |
|1648490101213          |Grace                  |Smith                  |
|1648490102210          |Grace                  |Fawcett                |
|1648490103210          |Heidi                  |Edison                 |
|1648490104210          |Frank                  |Fawcett                |
```

## Scalar functions

https://docs.ksqldb.io/en/latest/developer-guide/ksqldb-reference/scalar-functions/

## Format 

```sql
SELECT FORMAT_TIMESTAMP(from_unixtime(rowtime), 'yyyy-MM-dd' ) as createtime, firstname, lastname
FROM userprofile
emit changes
limit 5; 

+-------------------------+-------------------------+-------------------------+
|CREATETIME               |FIRSTNAME                |LASTNAME                 |
+-------------------------+-------------------------+-------------------------+
|2022-03-28               |Ivan                     |Fawcett                  |
|2022-03-28               |Grace                    |Smith                    |
|2022-03-28               |Grace                    |Fawcett                  |
|2022-03-28               |Heidi                    |Edison                   |
|2022-03-28               |Frank                    |Fawcett                  |
Limit Reached
Query terminated
```

```sql
SELECT FORMAT_TIMESTAMP(FROM_UNIXTIME(rowtime), 'yyyy-MM-dd' ) as create_time, CONCAT(firstname, ' ', UCASE(lastname)) as full_name
from  USERPROFILE 
EMIT CHANGES
LIMIT 5;
+--------------------------------------+--------------------------------------+
|CREATE_TIME                           |FULL_NAME                             |
+--------------------------------------+--------------------------------------+
|2022-03-28                            |Ivan FAWCETT                          |
|2022-03-28                            |Grace SMITH                           |
|2022-03-28                            |Grace FAWCETT                         |
|2022-03-28                            |Heidi EDISON                          |
|2022-03-28                            |Frank FAWCETT                         |
Limit Reached
Query terminated
```