# Introducing Tables

- A table in Kafka is the state “now"
- A messages for a table
    - updates the previous message in the set with the same key
    - adds a new message when there is no message with the same key
- Examples:
    - Stock level
    - Web traffic seen in a time period
    - Latest weather for a city

## Create a table with CSV
- Using the "COUNTRY-CSV" topic create the "COUNTRYTABLE" table
- Create the COUNTRYTABLE table using KSQL
  - Requirment to specify the KEY
  - How to specify the key within the COUNTRY-CSV topic
- What happens to the COUNTRYTABLE table for
  - Updates with the same key
   - New message when there is no message with the same key

### Create topic and produce records to it

```cmd
kafka-topics --bootstrap-server localhost:9092 --create --topic COUNTRY-CSV --partitions 1 --replication-factor
1
Created topic COUNTRY-CSV.
```
```cmd
kafka-console-producer --bootstrap-server localhost:9092 --topic COUNTRY-CSV --property "parse.key=true" --prope
rty "key.separator=:"
>AU:Australia
>IN:India
>GB:UK
>US:United States
```
### Create KSQL Table

```sql
ksql> CREATE TABLE COUNTRYTABLE  (countrycode VARCHAR PRIMARY KEY, countryname VARCHAR) WITH (KAFKA_TOPIC='COUNTRY-CSV', VALUE_FORMAT='DELIMITED');

 Message
---------------
 Table created
---------------

ksql> show tables;

 Table Name   | Kafka Topic | Key Format | Value Format | Windowed
-------------------------------------------------------------------
 COUNTRYTABLE | COUNTRY-CSV | KAFKA      | DELIMITED    | false
-------------------------------------------------------------------

ksql> describe COUNTRYTABLE;

Name                 : COUNTRYTABLE
 Field       | Type
----------------------------------------------
 COUNTRYCODE | VARCHAR(STRING)  (primary key)
 COUNTRYNAME | VARCHAR(STRING)
----------------------------------------------
For runtime statistics and query details run: DESCRIBE <Stream,Table> EXTENDED;

ksql> select * from countrytable emit changes;
+---------------------------+---------------------------+
|COUNTRYCODE                |COUNTRYNAME                |
+---------------------------+---------------------------+
|AU                         |Australia                  |
|IN                         |India                      |
|GB                         |UK                         |
|US                         |United States              |

ksql> select * from countrytable where countrycode='GB' emit changes limit 1;
+-------------------------+-------------------------+
|COUNTRYCODE              |COUNTRYNAME              |
+-------------------------+-------------------------+
|GB                       |UK                       |
Limit Reached
Query terminated
```
## Update table
Add 2 more records (1 insert and 1 update) with the cli consumer
```cmd
sh-4.4$ kafka-console-producer --bootstrap-server localhost:9092 --topic COUNTRY-CSV --property "parse.key=true" --property "key.separator=:"
>AU:Australia
>IN:India
>GB:UK
>US:United States
>GB:United Kingdom
>FR:France
```
Now query the table again and see the results. 
```sql
ksql> select countrycode, countryname from countrytable emit changes;
+--------------------------+--------------------------+
|COUNTRYCODE               |COUNTRYNAME               |
+--------------------------+--------------------------+
|AU                        |Australia                 |
|IN                        |India                     |
|GB                        |UK                        |
|US                        |United States             |
|GB                        |United Kingdom            |
|FR                        |France                    |
|GB                        |UK                        |
|GB                        |United Kingdom            |
```
When we query a table with EMIT CHANGES you are actually querying the changelog to the table. The kafka topic (which is how the changelog is represented) is eventually compacted to keep only the latest value for each key, but until then you will see the "duplicates". When this table is materialized (such as in a JOIN or AGGREAGTE), the semantics will be correct (you will only see one value for that key).