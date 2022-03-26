# Create a stream with JSON
- JSON
    - User profile in topic USERPROFILE
- Field specification
- Select by field and data type
- Find out what's happening in a stream

```json
{
      "userid": 1000,
      "firstname": "Alison",
      "lastname": "Smith",
      "countrycode": "GB",
      "rating": 4.7
}
```

## Create topic 
```cmd
kafka-topics --bootstrap-server localhost:9092 --create --topic USERPROFILE --partitions 1 --replication-factor
1
```

## Launch Consumer
```cmd
kafka-console-producer --bootstrap-server localhost:9092 --topic USERPROFILE << EOF
>
> {"userid": 1000, "firstname": "Alison", "lastname":"Smith", "countrycode":"GB", "rating":4.7}
>
> EOF
```

## Create Stream

```sql
ksql> CREATE STREAM userprofile (userid INT, firstname VARCHAR, lastname VARCHAR, countrycode VARCHAR, rating DOUBLE) \
>     WITH (VALUE_FORMAT = 'JSON', KAFKA_TOPIC= 'USERPROFILE');

 Message
----------------
 Stream created
----------------
```
## See datatypes in Stream

```sql
ksql> describe userprofile;

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

## Query the Stream
```sql
ksql> select userid, firstname, lastname, countrycode, rating from userprofile emit changes;
+-----------+-----------+-----------+-----------+-----------+
|USERID     |FIRSTNAME  |LASTNAME   |COUNTRYCODE|RATING     |
+-----------+-----------+-----------+-----------+-----------+
|1000       |Alison     |Smith      |GB         |4.7        |
```

Run the consumer again and insert a new user, we should see that the table above has been updated:

```sql
ksql> select userid, firstname, lastname, countrycode, rating from userprofile emit changes;
+-----------+-----------+-----------+-----------+-----------+
|USERID     |FIRSTNAME  |LASTNAME   |COUNTRYCODE|RATING     |
+-----------+-----------+-----------+-----------+-----------+
|1000       |Alison     |Smith      |GB         |4.7        |
|1001       |Bob        |Smith      |US         |4.2        |
```
