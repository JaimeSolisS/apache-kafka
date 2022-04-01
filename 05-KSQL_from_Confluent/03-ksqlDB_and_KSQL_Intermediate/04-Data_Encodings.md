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

Lauch a producer (the topic will be created automatically) 
```cmd
kafka-avro-console-producer --bootstrap-server localhost:9092 --topic COMPLAINTS_AVRO \
--property value.schema='
{
  "type": "record",
  "name": "myrecord",
  "fields": [
      {"name": "customer name", "type": "string" }
      {"name": "complaint_type", "type": "string" }
      {"name": "trip_cost", "type": "float" }
      {"name": "new customer", "type": "boolean"}
  ]
}'

>{"customer_name":"Carol", "complaint_type":"Late arrival", "trip_cost": 19.60, "new_customer": false}

```

Check if Topic was created

```cmd
kafka-topics --bootstrap-server localhost:9092 --list
```
Create Stream (do not need to specify columns because they are already defined within the data)
```sql
ksql> CREATE STREAM complaints_avro WITH (VALUE FORMAT = 'AVRO', KAFKA_TOPIC = 'COMPLAINTS_AVRO');

ksql> select * from complaints_avro emit changes;
```

Now Inject bad data into the producer 

```cmd
> {"customer_name":"Bad ata", "complaint_type":"Bad driver", "trip_cost": 22.40, "new_customer": ShouldBeABoolean}
```

