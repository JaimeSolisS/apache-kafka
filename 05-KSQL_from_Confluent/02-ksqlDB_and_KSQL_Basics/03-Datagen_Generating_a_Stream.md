# Datagen - Generating a stream

- KSQL Datagen
  - ksql-datagen command-line tool
  - Generate random test data
  - Use provided custom schema
  - We'll be generating USERPROFILE
- Options to generate a stream of
  - Formats : CSV, JSON, AVRO
  - Rate
  - Maximum number to produce

Create userprofile.json 

```json
{
  "namespace": "streams",
  "name": "userprofile",
  "type": "record",
  "fields": [
    {
      "name": "userid",
      "type": {
        "type": "string",
        "arg.properties": {
          "iteration": {
            "start": 1000,
            "step": 1
          }
        }
      }
    },
    {
      "name": "firstname",
      "type": {
        "type": "string",
        "arg.properties": {
          "options": [
            "Alice",
            "Bob",
            "Carol",
            "Dan",
            "Eve",
            "Frank",
            "Grace",
            "Heidi",
            "Ivan"
          ]
        }
      }
    },
    {
      "name": "lastname",
      "type": {
        "type": "string",
        "arg.properties": {
          "options": [
            "Smith",
            "Jones",
            "Coen",
            "Fawcett",
            "Edison",
            "Jones",
            "Dotty"
          ]
        }
      }
    },
    {
      "name": "countrycode",
      "type": {
        "type": "string",
        "arg.properties": {
          "options": [
            "AU",
            "IN",
            "GB",
            "US"
          ]
        }
      }
    },
    {
      "name": "rating",
      "type": {
        "type": "string",
        "arg.properties": {
          "options": [
            "3.4",
            "3.9",
            "2.2",
            "4.4",
            "3.7",
            "4.9"
          ]
        }
      }
    }
  ]
}

```

Execute ksql-datagen 
                                                                 
```cmd
ksql-datagen schema=userprofile.json format=json topic=USERPROFILE key=userid msgRate=1 iterations=100
```

I kept receiving the following errors because the bootstrap server by default is 9092:

```log
Error when sending message to topic: 'USERPROFILE', with key: '['1000']', and value: '[ '1000' | 'Grace' | 'Fawcett' | 'IN' | '3.9' ]'
org.apache.kafka.common.errors.TimeoutException: Topic USERPROFILE not present in metadata after 60000 ms.
```
So I had to add the bootstrap-server property manually. To run indefinitely just don't add iterations property :

```cmd
ksql-datagen schema=userprofile.json bootstrap-server=broker:29092 format=json topic=USERPROFILE key=userid msgRate=1
```

Now it produced 100 records:
```
['1000'] --> ([ '1000' | 'Ivan' | 'Fawcett' | 'GB' | '3.9' ]) ts:1648490100889
['1001'] --> ([ '1001' | 'Grace' | 'Smith' | 'US' | '4.9' ]) ts:1648490101213
['1002'] --> ([ '1002' | 'Grace' | 'Fawcett' | 'IN' | '4.4' ]) ts:1648490102210
['1003'] --> ([ '1003' | 'Heidi' | 'Edison' | 'AU' | '3.4' ]) ts:1648490103210
['1004'] --> ([ '1004' | 'Frank' | 'Fawcett' | 'IN' | '3.9' ]) ts:1648490104210
['1005'] --> ([ '1005' | 'Grace' | 'Jones' | 'US' | '2.2' ]) ts:1648490105210
...
```

```sql
ksql> print 'USERPROFILE' interval 10;
Key format: JSON or KAFKA_INT or KAFKA_STRING
Value format: JSON or KAFKA_STRING
rowtime: 2022/03/28 17:55:00.889 Z, key: 1000, value: {"userid":"1000","firstname":"Ivan","lastname":"Fawcett","countrycode":"GB","rating":"3.9"}, partition: 0
rowtime: 2022/03/28 17:55:10.210 Z, key: 1010, value: {"userid":"1010","firstname":"Frank","lastname":"Fawcett","countrycode":"GB","rating":"3.9"}, partition: 0
rowtime: 2022/03/28 17:55:20.210 Z, key: 1020, value: {"userid":"1020","firstname":"Dan","lastname":"Jones","countrycode":"AU","rating":"3.4"}, partition: 0
rowtime: 2022/03/28 17:55:30.210 Z, key: 1030, value: {"userid":"1030","firstname":"Eve","lastname":"Jones","countrycode":"GB","rating":"2.2"}, partition: 0
rowtime: 2022/03/28 17:55:40.210 Z, key: 1040, value: {"userid":"1040","firstname":"Alice","lastname":"Coen","countrycode":"GB","rating":"4.4"}, partition: 0
rowtime: 2022/03/28 17:55:50.210 Z, key: 1050, value: {"userid":"1050","firstname":"Grace","lastname":"Fawcett","countrycode":"AU","rating":"3.4"}, partition: 0
rowtime: 2022/03/28 17:56:00.210 Z, key: 1060, value: {"userid":"1060","firstname":"Carol","lastname":"Smith","countrycode":"GB","rating":"4.4"}, partition: 0
rowtime: 2022/03/28 17:56:10.210 Z, key: 1070, value: {"userid":"1070","firstname":"Heidi","lastname":"Jones","countrycode":"AU","rating":"4.9"}, partition: 0
rowtime: 2022/03/28 17:56:20.210 Z, key: 1080, value: {"userid":"1080","firstname":"Ivan","lastname":"Dotty","countrycode":"AU","rating":"3.7"}, partition: 0
rowtime: 2022/03/28 17:56:30.209 Z, key: 1090, value: {"userid":"1090","firstname":"Frank","lastname":"Edison","countrycode":"IN","rating":"3.9"}, partition: 0
```