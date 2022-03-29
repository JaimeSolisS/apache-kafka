# Joins

- A KSQL join merges data using a SQL like JOIN syntax.
- The result of a KSQL join is a new stream or table
- Streams and tables can be joined
    - Stream to streams... to create a new stream
    - Table to table .. to create a new table.
    - Stream and a table .. to create a new stream.

## Requirements for using joins
- Same partitioning ("co-partitioned") for join fields
- For tables
    - The KEY must VARCHAR or STRING
    - The Kafka message key must be the same as the contents of the column set in KEY

## First Join

- Join
    - Stream USERPROFILE
    - Table COUNTRYTABLE
- Stream and a table -> New Stream

```sql 
select up.firstname, up.lastname, up.countrycode, ct.countryname 
from USERPROFILE up
left join COUNTRYTABLE ct
on ct.countrycode = up.countrycode
emit changes;

+----------------+----------------+----------------+----------------+
|FIRSTNAME       |LASTNAME        |UP_COUNTRYCODE  |COUNTRYNAME     |
+----------------+----------------+----------------+----------------+
|Eve             |Edison          |AU              |Australia       |
|Ivan            |Fawcett         |US              |United States   |
|Dan             |Fawcett         |IN              |India           |
|Frank           |Coen            |IN              |India           |
|Alice           |Edison          |US              |United States   |
|Grace           |Fawcett         |AU              |Australia       |
|Heidi           |Edison          |AU              |Australia       |
|Alice           |Fawcett         |GB              |United Kingdom  |
|Frank           |Coen            |AU              |Australia       |
|Ivan            |Coen            |IN              |India           |
```

## Creat a stream 

```sql
create stream up_joined as
select up.rowtime as createtime, up.countrycode as countrycode, CONCAT(up.firstname, ' ', UCASE(up.lastname), ' from ', ct.countryname, ' has a rating of ', CAST(up.rating as VARCHAR), ' stars.') as description
from USERPROFILE up
left join COUNTRYTABLE ct
on up.countrycode = ct.countrycode; 

 Message
-----------------------------------------
 Created query with ID CSAS_UP_JOINED_33
-----------------------------------------

ksql> select * from up_joined emit changes limit 10;
+-------------------------------------------------------------+-------------------------------------------------------------+-------------------------------------------------------------+
|COUNTRYCODE                                                  |CREATETIME                                                   |DESCRIPTION                                                  |
+-------------------------------------------------------------+-------------------------------------------------------------+-------------------------------------------------------------+
|AU                                                           |1648577264372                                                |Eve EDISON from Australia has a rating of 4.9 stars.         |
|US                                                           |1648577264653                                                |Ivan FAWCETT from United States has a rating of 4.9 stars.   |
|IN                                                           |1648577265653                                                |Dan FAWCETT from India has a rating of 3.7 stars.            |
|IN                                                           |1648577266653                                                |Frank COEN from India has a rating of 4.9 stars.             |
|US                                                           |1648577267652                                                |Alice EDISON from United States has a rating of 4.9 stars.   |
|AU                                                           |1648577268652                                                |Grace FAWCETT from Australia has a rating of 3.9 stars.      |
|AU                                                           |1648577269652                                                |Heidi EDISON from Australia has a rating of 4.4 stars.       |
|GB                                                           |1648577270653                                                |Alice FAWCETT from United Kingdom has a rating of 3.4 stars. |
|AU                                                           |1648577271652                                                |Frank COEN from Australia has a rating of 3.4 stars.         |
|IN                                                           |1648577272653                                                |Ivan COEN from India has a rating of 4.9 stars.              |
```
# Check
Update a Country name and check if it is reflected after the join. 
```
kafka-console-producer --bootstrap-server localhost:9092 --topic COUNTRY-CSV --property "parse.key=true" --property "key.separator=:"
>US:United States of America
>GB:UK
```
```
ksql> select * from COUNTRYTABLE emit changes;
+----------------------------------+----------------------------------+
|COUNTRYCODE                       |COUNTRYNAME                       |
+----------------------------------+----------------------------------+
|AU                                |Australia                         |
|IN                                |India                             |
|GB                                |UK                                |
|US                                |United States                     |
|GB                                |United Kingdom                    |
|FR                                |France                            |
|US                                |United States of America          |
|GB                                |UK                                |
```
```
ksql> select up.firstname, up.lastname, up.countrycode, ct.countryname
>from USERPROFILE up
>left join COUNTRYTABLE ct
>on ct.countrycode = up.countrycode
>emit changes limit 10;
+----------------+----------------+----------------+----------------+
|FIRSTNAME       |LASTNAME        |UP_COUNTRYCODE  |COUNTRYNAME     |
+----------------+----------------+----------------+----------------+
|Eve             |Edison          |AU              |Australia       |
|Ivan            |Fawcett         |US              |United States   |
|Dan             |Fawcett         |IN              |India           |
|Frank           |Coen            |IN              |India           |
|Alice           |Edison          |US              |United States   |
|Grace           |Fawcett         |AU              |Australia       |
|Heidi           |Edison          |AU              |Australia       |
|Alice           |Fawcett         |GB              |United Kingdom  |
|Frank           |Coen            |AU              |Australia       |
|Ivan            |Coen            |IN              |India           |
Limit Reached
Query terminated
```
