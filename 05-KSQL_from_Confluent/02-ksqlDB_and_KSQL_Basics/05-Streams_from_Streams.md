# Streams from streams

- Streams are a great building block
- We can build a stream .. from a stream
- Using CASE statment

    ```
    select firstname,
           ucase(lastname),
           countrycode,
           rating
            case when rating < 2.5 then 'Poor'
                when rating between 2.5 and 4.2 then 'Good'
                else 'Excellent'
            end
    from userprofile;
    ```
## Streams managment 
- Scripts using RUN SCRIPT
- Drop stream
   - Conditional - if exists
   - Optional - delete topic
- Running streams
   - Find active streams
   - Show extended details & metrics
   - Terminate query

## Create KSQL Script

create user_profile_pretty.ksql
```sql
-------------------------------------------------------------
-- This file will generate the user_profile_pretty presentation
-------------------------------------------------------------
-- Change scope default to earliest
SET 'auto.offset.reset'='earliest';

-- generate a new stream from userprofile
create stream user_profile_pretty as
select CONCAT(firstname, ' ', 
              UCASE(lastname), ' from ',  
              COUNTRYCODE, ' has a rating of ',  
              CAST(rating as varchar), ' stars. ', 
              CASE WHEN rating <2.5 then 'Poor'
              		WHEN rating BETWEEN 2.5 and 4.2 then 'Good'
              		ELSE 'Excellent' 
              END) as description
              from  USERPROFILE;
```

run script:
```sql
ksql> run script 'user_profile_pretty.ksql';

 Message
--------------------------------------------------
 Created query with ID CSAS_USER_PROFILE_PRETTY_3
--------------------------------------------------

ksql> list streams;

 Stream Name         | Kafka Topic                 | Key Format | Value Format | Windowed
------------------------------------------------------------------------------------------
 KSQL_PROCESSING_LOG | default_ksql_processing_log | KAFKA      | JSON         | false
 USERPROFILE         | USERPROFILE                 | KAFKA      | JSON         | false
 USER_PROFILE_PRETTY | USER_PROFILE_PRETTY         | KAFKA      | JSON         | false
------------------------------------------------------------------------------------------
ksql> select description from user_profile_pretty emit changes limit 5;
+-----------------------------------------------------------------+
|DESCRIPTION                                                      |
+-----------------------------------------------------------------+
|Ivan FAWCETT from GB has a rating of 3.9 stars. Good             |
|Grace SMITH from US has a rating of 4.9 stars. Excellent         |
|Grace FAWCETT from IN has a rating of 4.4 stars. Excellent       |
|Heidi EDISON from AU has a rating of 3.4 stars. Good             |
|Frank FAWCETT from IN has a rating of 3.9 stars. Good            |
Limit Reached
Query terminated 
```
See contents of the stream. With this command we can see the description and the number of messages. 
```sql
ksql> describe user_profile_pretty extended;


Name                 : USER_PROFILE_PRETTY
Type                 : STREAM
Timestamp field      : Not set - using <ROWTIME>
Key format           : KAFKA
Value format         : JSON
Kafka topic          : USER_PROFILE_PRETTY (partitions: 1, replication: 1)
Statement            : CREATE STREAM USER_PROFILE_PRETTY WITH (KAFKA_TOPIC='USER_PROFILE_PRETTY', PARTITIONS=1, REPLICAS=1) AS SELECT CONCAT(USERPROFILE.FIRSTNAME, ' ', UCASE(USERPROFILE.LASTNAME), ' from ', USERPROFILE.COUNTRYCODE, ' has a rating of ', CAST(USERPROFILE.RATING AS STRING), ' stars. ', (CASE WHEN (USERPROFILE.RATING < 2.5) THEN 'Poor' WHEN (USERPROFILE.RATING BETWEEN 2.5 AND 4.2) THEN 'Good' ELSE 'Excellent' END)) DESCRIPTION
FROM USERPROFILE USERPROFILE
EMIT CHANGES;

 Field       | Type
-------------------------------
 DESCRIPTION | VARCHAR(STRING)
-------------------------------

Queries that write from this STREAM
-----------------------------------
CSAS_USER_PROFILE_PRETTY_15 (RUNNING) : CREATE STREAM USER_PROFILE_PRETTY WITH (KAFKA_TOPIC='USER_PROFILE_PRETTY', PARTITIONS=1, REPLICAS=1) AS SELECT CONCAT(USERPROFILE.FIRSTNAME, ' ', UCASE(USERPROFILE.LASTNAME), ' from ', USERPROFILE.COUNTRYCODE, ' has a rating of ', CAST(USERPROFILE.RATING AS STRING), ' stars. ', (CASE WHEN (USERPROFILE.RATING < 2.5) THEN 'Poor' WHEN (USERPROFILE.RATING BETWEEN 2.5 AND 4.2) THEN 'Good' ELSE 'Excellent' END)) DESCRIPTION FROM USERPROFILE USERPROFILE EMIT CHANGES;

For query topology and execution plan please run: EXPLAIN <QueryId>

Local runtime statistics
------------------------
messages-per-sec:      2.10   total-messages:       210     last-message: 2022-03-28T21:05:47.609Z

(Statistics of the local KSQL server interaction with the Kafka topic USER_PROFILE_PRETTY)

Consumer Groups summary:

Consumer Group       : _confluent-ksql-default_query_CSAS_USER_PROFILE_PRETTY_15

Kafka topic          : USERPROFILE
Max lag              : 0

 Partition | Start Offset | End Offset | Offset | Lag
------------------------------------------------------
 0         | 0            | 210        | 210    | 0
------------------------------------------------------
```

## Drop stream

```sql
ksql> drop stream user_profile_pretty;

ksql> list streams;

 Stream Name         | Kafka Topic                 | Key Format | Value Format | Windowed
------------------------------------------------------------------------------------------
 KSQL_PROCESSING_LOG | default_ksql_processing_log | KAFKA      | JSON         | false
 USERPROFILE         | USERPROFILE                 | KAFKA      | JSON         | false
------------------------------------------------------------------------------------------
```

If we get message that we need to terminate the query just execute the following command with the query ID:

```sql
ksql> terminate query CSAS_USER_PROFILE_PRETTY_3; 
```

If we don't know if the stream has already been dropped we can, in fact, say drop stream if it exists and that will conditionally drop the stream if it's there. And if it's not there, it won't give you an error.

```sql
ksql> drop stream IF EXISTS user_profile_pretty;
```

Another great extension to the drop stream command is the optional delete topic. And what this says is if the stream is to be dropped, you can also drop the created Kafka topic on your behalf.

```sql
ksql> drop stream IF EXISTS user_profile_pretty DELETE TOPIC;
```