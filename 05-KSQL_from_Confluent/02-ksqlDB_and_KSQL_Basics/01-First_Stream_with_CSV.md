# Create a stream with CSV

- Using topic USERS
- Create a stream using KSQL
- Work with consumer offsets
- Build an aggregate
- Introduce the "limit" clause 

# Push queries
- **Push** queries – constantly query & output results
  - We'll introduce to Pull queries in a later lesson after tables
- Push queries continue to output results until
  - Terminated by the user
  - Exceed LIMIT condition

- Push queries were the default in KSQL 5.3 and earlier
  - `EMIT CHANGES` indicates a query is a push query.
  - Required in KSQL against ksqlDB 5.4 onwards

```sql
-- KSQL 5.3 and earlier
select name, countrycode 
from users_stream; 
-- KSQL on ksqlDB 5.4 onwards. 
select name, countrycode 
from users_stream
emit changes; 
```

# Our first KSQL Stream

## Create
```sql
ksql> create stream users_stream (name VARCHAR, countrycode VARCHAR) WITH (KAFKA_TOPIC='USERS', VALUE_FORMAT='DELIMITED');

 Message
----------------
 Stream created
----------------
```
## List
```sql
ksql> list streams;

 Stream Name         | Kafka Topic                 | Key Format | Value Format | Windowed
------------------------------------------------------------------------------------------
 KSQL_PROCESSING_LOG | default_ksql_processing_log | KAFKA      | JSON         | false
 USERS_STREAM        | USERS                       | KAFKA      | DELIMITED    | false
------------------------------------------------------------------------------------------
```

We've always been using the current offset, which means we're always been showing data from this moment in time going forward. Change the default to set the offset to earliest.

```sql
ksql> SET 'auto.offset.reset'='earliest';
Successfully changed local property 'auto.offset.reset' to 'earliest'. Use the UNSET command to revert your change.
```
And now every time we interact against the Kafka topics within this KSQL CLI session, we've changed the default behavior to always listen to the first record or the beginning of time.

## Select and Limit
```sql
ksql> select name, countrycode from users_stream emit changes;
+---------------+--------------+
|NAME           |COUNTRYCODE   |
+---------------+--------------+
|Alice          |US            |
|Bob            |GB            |
|Carole         |AU            |
|Dan            |US            |
|Joe            |US            |

ksql> select name, countrycode from users_stream emit changes limit 4;
+---------------+--------------+
|NAME           |COUNTRYCODE   |
+---------------+--------------+
|Alice          |US            |
|Bob            |GB            |
|Carole         |AU            |
|Dan            |US            |
Limit Reached
Query terminated
```

## Aggregation 
```sql
ksql> select countrycode, count(*) from users_stream group by countrycode emit changes;
+----------------------------------+----------------------------------+
|COUNTRYCODE                       |KSQL_COL_0                        |
+----------------------------------+----------------------------------+
|US                                |1                                 |
|GB                                |1                                 |
|AU                                |1                                 |
|US                                |2                                 |
|US                                |3                                 |
```

## Drop Stream

```sql
ksql> drop stream if exists users_stream;

 Message
---------------------------------------------------
 Source `USERS_STREAM` (topic: USERS) was dropped.
---------------------------------------------------
```
 We can also delete the topic from this step with

 ```sql
 drop stream if exists users_stream delete topic;
 ```