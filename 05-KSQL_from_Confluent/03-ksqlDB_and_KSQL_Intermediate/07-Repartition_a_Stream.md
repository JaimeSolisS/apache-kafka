# Repartition a Stream

- Joins require the columns involved in the join
   - Same partitioning ("co-partitioned") for join fields
- But .. What if it isn't?
   - Why does this matter?
   - How do we fix it?
- Let's add driver profiles
   - It's built with a different number of partitions
   - Let's re-partition a stream

Create topic
```cmd
kafka-topics --bootstrap-server localhost:9092 --create --topic DRIVER_PROFILE --partitions 2 --replication-factor 1
```

Launch consumer
```cmd
 kafka-console-producer --bootstrap-server localhost:9092 --topic DRIVER_PROFILE
>{"driver_name":"Mr. Speedy", "countrycode":"AU", "rating":2.4}
```
Create Stream
```sql
ksql>CREATE STREAM DRIVER_PROFILE (driver_name VARCHAR, countrycode VARCHAR, rating DOUBLE)
WITH (KAFKA_TOPIC='DRIVER_PROFILE', VALUE_FORMAT='JSON'); 

 Message
----------------
 Stream created
----------------
```
Now try to join it with countrytable

```sql
ksql> select dp.driver_name, ct.countrycode, dp.rating
from DRIVER_PROFILE dp
left join COUNTRYTABLE ct
on ct.countrycode=dp.countrycode
emit changes; 

Can't join `DP` with `CT` since the number of partitions don't match. `DP` partitions = 2; `CT` partitions = 1. Please repartition either one so that the number of partitions match.
```
```sql
ksql>create stream driverprofile_rekeyed with (partitions=1) 
as select *
from DRIVER_PROFILE 
partition by driver_name;

 Message
------------------------------------------------------
 Created query with ID CSAS_DRIVERPROFILE_REKEYED_109
------------------------------------------------------
```
To validate this, use describe extended. Now try again. 

```sql
ksql> select dp.driver_name, ct.countrycode, dp.rating
from DRIVERPROFILE_REKEYED dp
left join COUNTRYTABLE ct
on ct.countrycode=dp.countrycode
emit changes; 
+----------------+----------------+----------------+
|DRIVER_NAME     |CT_COUNTRYCODE  |RATING          |
+----------------+----------------+----------------+
|Mr. Speedy      |AU              |2.4             |
```