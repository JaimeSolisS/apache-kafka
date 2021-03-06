# Data Formats

- Data comes in different formats.
- KSQL supports three serialization mechisms
    - Comma-separated (CSV, or "delimited")
        ```
        Alice, Late arrival, 43.10, true
        ```
    - JSON
        ```
        "customer name":"Alice",
        "complaint type": "Late arrival",
        "trip cost": 43.10,
        "new customer": true
        ```
    - AVRO
        ```
        JSON plus Schema
      [{ "name": "customer name", "type": "string" }',
      {"name": "complaint type", "type": "string" }',
      {"name": "trip cost", "type": "float"},
      {"name": "new customer", "type": "boolean"} ]
        ```

# Handling Complaints 
- Design the complaints stream to handle unhappy customers.

| Column        | Example Data | AVRO Type | KSQL Type |
|---------------|--------------|-----------|-----------|
|customer_name  | Alice        | string    | VARCHAR   |
|complaint_type | Late arrival | string    | VARCHAR   |
|trip_cost      | 43.10        | float     | DOUBLE    |
|new_customer   | true         | boolean   | BOOLEAN   |

# CSV Delimited Data

Create topic
```cmd
kafka-topics --bootstrap-server localhost:9092 --create --topic COMPLAINTS_CSV --partitions 1 --replication-factor 1
```

Create Stream 
```sql
ksql> CREATE STREAM complaints_csv (customer_name VARCHAR, complaint_type VARCHAR, trip_cost DOUBLE, new_customer BOOLEAN) WITH (VALUE_FORMAT = 'DELIMITED', KAFKA_TOPIC = 'COMPLAINTS_CSV');

ksql> select * from complaints_csv emit changes;
```
Populate topic
```cmd
kafka-console-producer --bootstrap-server localhost:9092 --topic COMPLAINTS_CSV
>Alice, Late arrival, 43.10, true
```

So far so good, but if we add more costumers in the same record we'll get some errors in log. 
```
Alice, Bob and Carole, Late arrival, 43.10, true
```

```log
org.apache.kafka.common.errors.SerializationException: Exception in deserializing the delimited row: Alice, Bob and Carole, Bad driver, 43.10, true
Caused by: io.confluent.ksql.util.KsqlException: Unexpected field count, csvFields:5 schemaFields:4 line: Alice, Bob and Carole, Bad driver, 43.10, true
```
# JSON Data
Create topic
```cmd
kafka-topics --bootstrap-server localhost:9092 --create --topic COMPLAINTS_JSON --partitions 1 --replication-factor 1
```
Create Stream 
```sql
ksql> CREATE STREAM complaints_json (customer_name VARCHAR, complaint_type VARCHAR, trip_cost DOUBLE, new_customer BOOLEAN) WITH (VALUE_FORMAT = 'JSON', KAFKA_TOPIC = 'COMPLAINTS_JSON');

ksql> select * from complaints_json emit changes;
```
Populate topic and Insert bad data on purpose 
```cmd
kafka-console-producer --bootstrap-server localhost:9092 --topic COMPLAINTS_JSON
> {"customer_name": "Alice, Bob and Carole", "complaint_type":"Bad_driver", "trip_cost": 22.40, "new_customer": true}
>{"customer_name":"Bad Data", "complaint_type":"Bad driver", "trip_cost": 22.40, "new_customer": ShouldBeABoolean}
```

Check the logs
```log
Caused by: com. fasterxml.jackson.core.JsonParseException: Unrecognized token 'ShouldBeABoolean': was expecting ('true', 'false' or 'null') at [Source: (byte[])"{"customer_name":"Bad Data", "complaint_type":"Bad driver" "trip_cost": 22.40, "new_customer": ShouldBeABoolean}"; line: 1, column: 114]
```

# AVRO

- Relies on schemas.
- All data has an associated schema is stored with it
- KSQL relies on the Confluent schema registry
- Avro "looks" like JSON format but is stored in binary
- When Avro data is read, the schema used when writing it is used to deserialize it
- Schemas can change - "evolve"

```json
Schema
{
  "type": "record",
  "name": "myrecord",
  "fields": [
      {"name": "customer name", "type": "string" }
      {"name": "complaint_type", "type": "string" }
      {"name": "trip_cost", "type": "float" }
      {"name": "new customer", "type": "boolean"}
  ]
}
```

Lauch a producer (the topic will be created automatically). If you can't find the command, check under use confluentinc/cp-schema-registry (hence the hostname is broker and not localhost).

```cmd
kafka-avro-console-producer --bootstrap-server broker:29092 --topic COMPLAINTS_AVRO \
--property value.schema='
{
  "type": "record",
  "name": "myrecord",
  "fields": [
      {"name": "customer_name", "type": "string" },
      {"name": "complaint_type", "type": "string" },
      {"name": "trip_cost", "type": "float" },
      {"name": "new_customer", "type": "boolean"}
  ]
}'
{"customer_name":"Carol", "complaint_type":"Late arrival", "trip_cost": 19.60, "new_customer": false}
```
 I noticed my avro producer didn't have a '>' character to specify it was accepting messages, so I spent quite a few time thinking it wasn't working and researching for a fix. Until I found this thread https://stackoverflow.com/questions/39625778/kafka-avro-console-producer-quick-start-fails  

Check if Topic was created

```cmd
kafka-topics --bootstrap-server localhost:9092 --list
COMPLAINTS_AVRO
COMPLAINTS_CSV
COMPLAINTS_JSON
COUNTRY-CSV
COUNTRYDRIVERS
USERPROFILE
```
Create Stream (do not need to specify columns because they are already defined within the data)
```sql
ksql> CREATE STREAM complaints_avro WITH (VALUE_FORMAT = 'AVRO', KAFKA_TOPIC = 'COMPLAINTS_AVRO');
 Message
----------------
 Stream created
----------------

ksql> select * from complaints_avro emit changes;
+----------------------------+----------------------------+----------------------------+----------------------------+
|CUSTOMER_NAME               |COMPLAINT_TYPE              |TRIP_COST                   |NEW_CUSTOMER                |
+----------------------------+----------------------------+----------------------------+----------------------------+
|Carol                       |Late arrival                |19.600000381469727          |false                       |
```

Now Inject bad data into the producer 

```cmd
{"customer_name":"Bad ata", "complaint_type":"Bad driver", "trip_cost": 22.40, "new_customer": ShouldBeABoolean}
Caused by: com.fasterxml.jackson.core.JsonParseException: Unrecognized token 'ShouldBeABoolean': was expecting (JSON String, Number, Array, Object or token 'null', 'true' or 'false')
...
```

# Schema Evolution with AVRO

- Add a field to our complaints schema
    - Number of rides taken

Launch a new producer to the same topic that was created previously defining a sligthly different schema

```cmd
kafka-avro-console-producer --bootstrap-server broker:29092 --topic COMPLAINTS_AVRO \
--property value.schema='
{
  "type": "record",
  "name": "myrecord",
  "fields": [
      {"name": "customer_name", "type": "string" },
      {"name": "complaint_type", "type": "string" },
      {"name": "trip_cost", "type": "float" },
      {"name": "new_customer", "type": "boolean"}, 
      {"name": "number_of_rides", "type": "int", "default": 1}
  ]
}'
{"customer_name":"Ed", "complaint_type":"Dirty car", "trip_cost": 29.10, "new_customer": false, "number_of_rides":22}
```
Now you can check the schema's versions on Confluent Control Center.  
```
ksql> select * from complaints_avro emit changes;
+-----------------------------------+-----------------------------------+-----------------------------------+-----------------------------------+
|CUSTOMER_NAME                      |COMPLAINT_TYPE                     |TRIP_COST                          |NEW_CUSTOMER                       |
+-----------------------------------+-----------------------------------+-----------------------------------+-----------------------------------+
|Carol                              |Late arrival                       |19.600000381469727                 |false                              |
|Ed                                 |Dirty car                          |29.100000381469727                 |false                              |
```
But the stream only shows 4 fields against its record. It is because we did it against version 1 of the schema. So we need to create a new one. 

```sql
CREATE STREAM complaints_avro_v2 WITH (kafka_topic='COMPLAINTS_AVRO', value_format='AVRO'); 
 Message
----------------
 Stream created
----------------
ksql> select * from complaints_avro_v2 emit changes;
+---------------------------+---------------------------+---------------------------+---------------------------+---------------------------+
|CUSTOMER_NAME              |COMPLAINT_TYPE             |TRIP_COST                  |NEW_CUSTOMER               |NUMBER_OF_RIDES            |
+---------------------------+---------------------------+---------------------------+---------------------------+---------------------------+
|Carol                      |Late arrival               |19.600000381469727         |false                      |null                       |
|Ed                         |Dirty car                  |29.100000381469727         |false                      |22                         |
Query terminated
ksql> list streams;

 Stream Name         | Kafka Topic                 | Key Format | Value Format | Windowed
------------------------------------------------------------------------------------------
 COMPLAINTS_AVRO     | COMPLAINTS_AVRO             | KAFKA      | AVRO         | false
 COMPLAINTS_AVRO_V2  | COMPLAINTS_AVRO             | KAFKA      | AVRO         | false
 COMPLAINTS_CSV      | COMPLAINTS_CSV              | KAFKA      | DELIMITED    | false
 COMPLAINTS_JSON     | COMPLAINTS_JSON             | KAFKA      | JSON         | false
 DRIVERLOCATIONS     | driverlocations             | KAFKA      | JSON         | false
 KSQL_PROCESSING_LOG | default_ksql_processing_log | KAFKA      | JSON         | false
 USERPROFILE         | USERPROFILE                 | KAFKA      | JSON         | false
------------------------------------------------------------------------------------------
```
