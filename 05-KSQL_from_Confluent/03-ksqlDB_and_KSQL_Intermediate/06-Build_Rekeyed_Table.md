# Build a rekeyed table

- For tables lookups
    - The Kafka `message key` must be the same as the `contents` of the column set in KEY

- But What if it isn't? We can rekey it

```sql
ksql> create stream weather_raw with (value_format='AVRO') as
>SELECT city->name AS city_name, city->country AS city_country, city->latitude as latitude, city->longitude as longitude, description, rain
>from weather;

 Message
-------------------------------------------
 Created query with ID CSAS_WEATHER_RAW_23
-------------------------------------------

ksql> create stream weather_keyed as
select * from weather_raw
partition by city_name;

 Message
---------------------------------------------
 Created query with ID CSAS_WEATHER_KEYED_25
---------------------------------------------

ksql> select * from weather_keyed emit changes limit 5;
+---------------------------+---------------------------+---------------------------+---------------------------+---------------------------+---------------------------+
|CITY_NAME                  |CITY_COUNTRY               |LATITUDE                   |LONGITUDE                  |DESCRIPTION                |RAIN                       |
+---------------------------+---------------------------+---------------------------+---------------------------+---------------------------+---------------------------+
|Sydney                     |AU                         |-33.8688                   |151.2093                   |light rain                 |1.25                       |
|Seattle                    |US                         |47.6062                    |-122.3321                  |heavy rain                 |7.0                        |
|San Francisco              |US                         |37.7749                    |-122.4194                  |fog                        |10.0                       |
|San Jose                   |US                         |37.3382                    |-121.8863                  |light rain                 |3.0                        |
|Fresno                     |US                         |36.7378                    |-119.7871                  |heavy rain                 |6.0                        |
Limit Reached
Query terminated
ksql> describe weather_keyed extended;

Name                 : WEATHER_KEYED
Type                 : STREAM
Timestamp field      : Not set - using <ROWTIME>
Key format           : KAFKA
Value format         : AVRO
Kafka topic          : WEATHER_KEYED (partitions: 1, replication: 1)
Statement            : CREATE STREAM WEATHER_KEYED WITH (KAFKA_TOPIC='WEATHER_KEYED', PARTITIONS=1, REPLICAS=1) AS SELECT *
FROM WEATHER_RAW WEATHER_RAW
PARTITION BY WEATHER_RAW.CITY_NAME
EMIT CHANGES;

 Field        | Type
---------------------------------------
 CITY_NAME    | VARCHAR(STRING)  (key)
 CITY_COUNTRY | VARCHAR(STRING)
 LATITUDE     | DOUBLE
 LONGITUDE    | DOUBLE
 DESCRIPTION  | VARCHAR(STRING)
 RAIN         | DOUBLE
---------------------------------------

Queries that write from this STREAM
-----------------------------------
CSAS_WEATHER_KEYED_25 (RUNNING) : CREATE STREAM WEATHER_KEYED WITH (KAFKA_TOPIC='WEATHER_KEYED', PARTITIONS=1, REPLICAS=1) AS SELECT * FROM WEATHER_RAW WEATHER_RAW PARTITION BY WEATHER_RAW.CITY_NAME EMIT CHANGES;

For query topology and execution plan please run: EXPLAIN <QueryId>

Local runtime statistics
------------------------
messages-per-sec:      0.13   total-messages:        13     last-message: 2022-04-01T23:40:46.455Z

(Statistics of the local KSQL server interaction with the Kafka topic WEATHER_KEYED)

Consumer Groups summary:

Consumer Group       : _confluent-ksql-default_query_CSAS_WEATHER_KEYED_25

Kafka topic          : WEATHER_RAW
Max lag              : 0

 Partition | Start Offset | End Offset | Offset | Lag
------------------------------------------------------
 0         | 0            | 13         | 13     | 0
------------------------------------------------------
```
We can see that city_name is key as a varchar. 
```
 Field        | Type
---------------------------------------
 CITY_NAME    | VARCHAR(STRING)  (key)
 CITY_COUNTRY | VARCHAR(STRING)
 LATITUDE     | DOUBLE
 LONGITUDE    | DOUBLE
 DESCRIPTION  | VARCHAR(STRING)
 RAIN         | DOUBLE
---------------------------------------
```

Our goal for rekeying, this stream was to ultimately create a table so we would always have the latest information about the weather in an individual city.

```sql
ksql> create table weather_now (city_name VARCHAR PRIMARY KEY) with (kafka_topic='WEATHER_KEYED', value_format='AVRO');

 Message
---------------
 Table created
---------------
ksql> select * from weather_now where city_name = 'San Diego' emit changes;
+-------------+-------------+-------------+-------------+-------------+-------------+
|CITY_NAME    |CITY_COUNTRY |LATITUDE     |LONGITUDE    |DESCRIPTION  |RAIN         |
+-------------+-------------+-------------+-------------+-------------+-------------+
|San Diego    |US           |32.7157      |-117.1611    |fog          |2.0          |
```

Let's see if we can actually alter the weather.

Create demo-weather-changes.json file and inject it into the topic. 
```cmd
cat demo-weather-changes.json | kafka-console-producer --broker-list localhost:9092 --topic WEATHER_NESTED
```

```sql
ksql> select * from weather_now where city_name = 'San Diego' emit changes;
+-------------+-------------+-------------+-------------+-------------+-------------+
|CITY_NAME    |CITY_COUNTRY |LATITUDE     |LONGITUDE    |DESCRIPTION  |RAIN         |
+-------------+-------------+-------------+-------------+-------------+-------------+
|San Diego    |US           |32.7157      |-117.1611    |fog          |2.0          |
|San Diego    |US           |32.7157      |-117.1611    |SUNNY        |2.0          |
```