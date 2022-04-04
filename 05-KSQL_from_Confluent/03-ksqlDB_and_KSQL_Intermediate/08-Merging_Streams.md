# Merging streams: INSERT INTO

- Combine streams together
    - `INSERT INTO` some-stream `SELECT from` another-stream
- Must be identical schema
- You can combine 2 .. or more!
- Tip: you can a pseudo-column to help with "origin"


We've gone global and want to combine our booking
applications:
  - Stream of requested rides in Europe using data gen
  - Stream of requested rides in USA using data gen
  - Want to know the origin of the data in combined stream
- Combine into single stream of all requested rides using INSERT

We're going to simulate rides with ksql-datagen. 

Create riderequest-europe.avro file

```json
{
  "namespace": "streams",
  "name": "riderequest",
  "type": "record",
  "fields": [
    {
      "name": "requesttime",
      "type": {
        "type": "long",
        "format_as_time": "unix_long",
        "arg.properties": {
          "iteration": {
            "start": 1,
            "step": 10
          }
        }
      }
    },
    {
      "name": "latitude",
      "type": {
        "type": "double",
        "arg.properties": {
          "range": {"min": 49.5, "max": 53.5}
        }
      }
    },     
    {
      "name": "longitude",
      "type": {
        "type": "double",
        "arg.properties": {
          "range": {"min": -2.0, "max": 2.0}
        }
      }
    },    
    {
      "name": "rideid",
      "type": {
        "type": "string",
        "arg.properties": {
          "regex": "ride_[1-9][0-9][0-9]"
        }
      }
    },
    {
      "name": "user",
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
      "name": "city_name",
      "type": {
        "type": "string",
        "arg.properties": {
          "options": [
            "Birmingham",
            "London",
            "London",
            "London",
            "London",
            "London",
            "London",
            "Manchester",
            "Bristol",
            "Newcastle",
            "Liverpool"
          ]
        }
      }
    }
  ]
}

```
Create riderequest-america.avro file
```json
{
  "namespace": "streams",
  "name": "riderequest",
  "type": "record",
  "fields": [
    {
      "name": "requesttime",
      "type": {
        "type": "long",
        "format_as_time": "unix_long",
        "arg.properties": {
          "iteration": {
            "start": 1,
            "step": 10
          }
        }
      }
    },
    {
      "name": "latitude",
      "type": {
        "type": "double",
        "arg.properties": {
          "range": {"min": 37.7 , "max": 45.0}
        }
      }
    },     
    {
      "name": "longitude",
      "type": {
        "type": "double",
        "arg.properties": {
          "range": {"min": -122.0, "max": -100.0}
        }
      }
    },    
    {
      "name": "rideid",
      "type": {
        "type": "string",
        "arg.properties": {
          "regex": "ride_[1-9][0-9][0-9]"
        }
      }
    },
    {
      "name": "user",
      "type": {
        "type": "string",
        "arg.properties": {
          "options": [
            "Judy",
            "Mike",
            "Niaj",
            "Oscar",
            "Peggy",
            "Sybil",
            "Ted",
            "Trudy",
            "Walter",
            "Wendy"
          ]
        }
      }
    },
    {
      "name": "city_name",
      "type": {
        "type": "string",
        "arg.properties": {
          "options": [
            "Seattle",
            "San Francisco",
            "San Francisco",
            "San Francisco",
            "San Francisco",
            "San Francisco",
            "San Francisco",
            "San Jose",
            "Fresno",
            "Los Angeles",
            "San Diego"
          ]
        }
      }
    }
  ]
}
```
Launch datagen and write data to two topics. The Avro examples were not working for me. Then I run into an article on stackoverflow. Basically you need to set additional parameter on the ksql-datagen command "schemaRegistryUrl=http://localhost:8081" or whichever hostname and/or port you have schema-registry running. 

```cmd
ksql-datagen schema=riderequest-europe.avro bootstrap-server=broker:29092 format=avro topic=riderequest-europe key=rideid msgRate=1 iterations=1000 schemaRegistryUrl=http://schema-registry:8081

['ride_494'] --> ([ 1649106794848L | 49.94403363427503 | -1.1646022306916195 | 'ride_494' | 'Heidi' | 'Manchester' ]) ts:1649106795128
['ride_999'] --> ([ 1649106795609L | 51.0983629508126 | -1.827947047949655 | 'ride_999' | 'Alice' | 'Birmingham' ]) ts:1649106795610
['ride_144'] --> ([ 1649106796607L | 51.44045630184282 | 1.5232182033854325 | 'ride_144' | 'Dan' | 'London' ]) ts:1649106796608
['ride_997'] --> ([ 1649106797607L | 51.06431752748189 | -0.4527493402638445 | 'ride_997' | 'Heidi' | 'London' ]) ts:1649106797608
```
```cmd
ksql-datagen schema=riderequest-america.avro bootstrap-server=broker:29092 format=avro topic=riderequest-america key=rideid msgRate=1 iterations=1000 schemaRegistryUrl=http://schema-registry:8081

['ride_702'] --> ([ 1649106938287L | 41.673720188244644 | -105.5415586352301 | 'ride_702' | 'Oscar' | 'San Diego' ]) ts:1649106938704
['ride_383'] --> ([ 1649106939063L | 38.99168441455035 | -102.2990916738202 | 'ride_383' | 'Trudy' | 'San Francisco' ]) ts:1649106939064
['ride_941'] --> ([ 1649106940063L | 39.11083559785268 | -118.13843789850208 | 'ride_941' | 'Peggy' | 'San Jose' ]) ts:1649106940063
['ride_810'] --> ([ 1649106941063L | 40.1823221834754 | -115.90100777373127 | 'ride_810' | 'Trudy' | 'San Francisco' ]) ts:1649106941063
['ride_362'] --> ([ 1649106942063L | 38.944161661413844 | -107.17608920202575 | 'ride_362' | 'Ted' | 'Los Angeles' ]) ts:1649106942063
```

Create streams for american and european topics. 

```sql
ksql> create stream rr_america_raw with (kafka_topic='riderequest-america', value_format='avro'); 
 Message
----------------
 Stream created
----------------
ksql> select * from rr_america_raw emit changes limit 5;
+-------------------+-------------------+-------------------+-------------------+-------------------+-------------------+
|REQUESTTIME        |LATITUDE           |LONGITUDE          |RIDEID             |USER               |CITY_NAME          |
+-------------------+-------------------+-------------------+-------------------+-------------------+-------------------+
|1649106938287      |41.673720188244644 |-105.5415586352301 |ride_702           |Oscar              |San Diego          |
|1649106939063      |38.99168441455035  |-102.2990916738202 |ride_383           |Trudy              |San Francisco      |
|1649106940063      |39.11083559785268  |-118.13843789850208|ride_941           |Peggy              |San Jose           |
|1649106941063      |40.1823221834754   |-115.90100777373127|ride_810           |Trudy              |San Francisco      |
|1649106942063      |38.944161661413844 |-107.17608920202575|ride_362           |Ted                |Los Angeles        |
Limit Reached
Query terminated
```
```sql
ksql> create stream rr_europe_raw with (kafka_topic='riderequest-europe', value_format='avro'); 
 Message
----------------
 Stream created
----------------
ksql> select * from rr_europe_raw emit changes limit 5;
+-------------------+-------------------+-------------------+-------------------+-------------------+-------------------+
|REQUESTTIME        |LATITUDE           |LONGITUDE          |RIDEID             |USER               |CITY_NAME          |
+-------------------+-------------------+-------------------+-------------------+-------------------+-------------------+
|1649106794848      |49.94403363427503  |-1.1646022306916195|ride_494           |Heidi              |Manchester         |
|1649106795609      |51.0983629508126   |-1.827947047949655 |ride_999           |Alice              |Birmingham         |
|1649106796607      |51.44045630184282  |1.5232182033854325 |ride_144           |Dan                |London             |
|1649106797607      |51.06431752748189  |-0.4527493402638445|ride_997           |Heidi              |London             |
|1649106798608      |50.128003719545255 |1.5059161169891966 |ride_768           |Frank              |London             |
```

Combine worldwide stream. `select 'Europe' as data_source` will put a pseudo column 'data_source' at the front of it with the constant 'Europe'. 

```sql
ksql> create stream rr_world as 
select 'Europe' as data_source, * 
from rr_europe_raw; 
 Message
-----------------------------------------
 Created query with ID CSAS_RR_WORLD_115
-----------------------------------------
```

Now insert data from america 

```sql
ksql> insert into rr_world 
select 'America' as data_source, *
from rr_america_raw; 
 Message
---------------------------------------
 Created query with ID INSERTQUERY_117
---------------------------------------
```

Check data is comming from both topics. 

```sql
ksql> select * from rr_world emit changes; 
+--------------------+--------------------+--------------------+--------------------+--------------------+--------------------+--------------------+
|DATA_SOURCE         |REQUESTTIME         |LATITUDE            |LONGITUDE           |RIDEID              |USER                |CITY_NAME           |
+--------------------+--------------------+--------------------+--------------------+--------------------+--------------------+--------------------+
|America             |1649108281571       |39.30617910310715   |-106.5699457375725  |ride_549            |Peggy               |San Francisco       |
|Europe              |1649108282273       |52.249488883719096  |-1.331591222341903  |ride_807            |Grace               |London              |
|America             |1649108282571       |44.245467478976614  |-103.4541788510661  |ride_925            |Ted                 |San Diego           |
|Europe              |1649108283272       |50.23176872365443   |0.340962348659529   |ride_453            |Ivan                |London              |
|America             |1649108283571       |43.98793141199166   |-116.4935061930086  |ride_742            |Peggy               |San Francisco       |
|Europe              |1649108284273       |50.29677107171275   |0.9074450398541312  |ride_279            |Alice               |London              |
|America             |1649108284571       |38.48932720057239   |-120.95748019941581 |ride_888            |Niaj                |San Francisco       |
|Europe              |1649108285272       |52.43600768751792   |0.5898487494216602  |ride_963            |Frank               |Bristol             |
|America             |1649108285571       |39.63278424057558   |-118.76681447246959 |ride_450            |Peggy               |San Francisco       |
|Europe              |1649108286273       |51.03348071192032   |0.04738234949392339 |ride_901            |Carol               |Newcastle           |
|America             |1649108286571       |38.10114250559576   |-121.4463181717711  |ride_680            |Sybil               |San Francisco       |
```