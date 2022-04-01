# Nested JSON 
- KSQL supports both flat and nested data structures.
- KSQL supports the STRUCT data type to access nested data structures.

```json
{
"city": {
    "name": "Sydney",
    "country": "AU",
    "latitude": -33.8688,
    "longitude": 151.2093
},
"description": "light rain",
"clouds": 92,
"deg": 26,
"humidity": 94,
"pressure": 1025.12,
"rain": 1.25
}
```
KSQL Nested JSON Syntax
```sql
city STRUCT <
  name VARCHAR,
  country VARCHAR,
  latitude DOUBLE,
  longitude DOUBLE
>
```

Create a Topic
```cmd
kafka-topics bootstrap-server localhost:9092 --create --topic WEATHER_NESTED --partitions 1 --replication-factor 1
```
Create demo-weather.json file and pipe it in to Kafka console producer directly into the weather topic. 

```json
{  "city": {    "name": "Sydney",    "country": "AU", "latitude":-33.8688, "longitude":151.2093 },  "description": "light rain",  "clouds": 92,  "deg": 26,  "humidity": 94,  "pressure": 1025.12,  "rain": 1.25  }
{  "city": {    "name": "Seattle",    "country": "US", "latitude":47.6062, "longitude":-122.3321 },  "description": "heavy rain",  "clouds": 92,  "deg": 19,  "humidity": 94,  "pressure": 1025.12,  "rain": 7  }
{  "city": {    "name": "San Francisco",    "country": "US", "latitude":37.7749, "longitude":-122.4194 },  "description": "fog",  "clouds": 92,  "deg": 19,  "humidity": 94,  "pressure": 1025.12,  "rain": 10  }
{  "city": {    "name": "San Jose",    "country": "US", "latitude":37.3382, "longitude":-121.8863 },  "description": "light rain",  "clouds": 92,  "deg": 23,  "humidity": 94,  "pressure": 1025.12,  "rain": 3  }
{  "city": {    "name": "Fresno",    "country": "US", "latitude":36.7378, "longitude":-119.7871 },  "description": "heavy rain",  "clouds": 92,  "deg": 22,  "humidity": 94,  "pressure": 1025.12,  "rain": 6  }
{  "city": {    "name": "Los Angeles",    "country": "US", "latitude":34.0522, "longitude":-118.2437 },  "description": "haze",  "clouds": 92,  "deg": 19,  "humidity": 94,  "pressure": 1025.12,  "rain": 2  }
{  "city": {    "name": "San Diego",    "country": "US", "latitude":32.7157, "longitude":-117.1611 },  "description": "fog",  "clouds": 92,  "deg": 19,  "humidity": 94,  "pressure": 1025.12,  "rain": 2  }
{  "city": {    "name": "Birmingham",    "country": "UK", "latitude":52.4862, "longitude":-1.8904 },  "description": "light rain",  "clouds": 92,  "deg": 26,  "humidity": 94,  "pressure": 1025.12,  "rain": 4  }
{  "city": {    "name": "London",    "country": "GB", "latitude":51.5074, "longitude":-0.1278 },  "description": "heavy rain",  "clouds": 92,  "deg": 19,  "humidity": 94,  "pressure": 1025.12,  "rain": 8  }
{  "city": {    "name": "Manchester",    "country": "GB", "latitude":53.4808, "longitude":-2.2426 },  "description": "fog",  "clouds": 92,  "deg": 26,  "humidity": 94,  "pressure": 1025.12,  "rain": 3  }
{  "city": {    "name": "Bristol",    "country": "GB", "latitude":51.4545, "longitude":-2.5879 },  "description": "light rain",  "clouds": 92,  "deg": 19,  "humidity": 94,  "pressure": 1025.12,  "rain": 3  }
{  "city": {    "name": "Newcastle",    "country": "GB", "latitude":54.9783, "longitude":-1.6178 },  "description": "heavy rain",  "clouds": 92,  "deg": 19,  "humidity": 94,  "pressure": 1025.12,  "rain": 12  }
{  "city": {    "name": "Liverpool",    "country": "GB", "latitude":53.4084, "longitude":-2.9916 },  "description": "haze",  "clouds": 92,  "deg": 23,  "humidity": 94,  "pressure": 1025.12,  "rain": 3  }
```

```cmd
cat demo-weather.json | kafka-console-producer --broker-list localhost:9092 --topic WEATHER_NESTED
```

Create Stream around the nested data structure
```sql 
ksql> CREATE STREAM weather 
      (city STRUCT <name VARCHAR, country VARCHAR, latitude DOUBLE, longitude DOUBLE>, 
      description VARCHAR, clouds BIGINT, deg BIGINT, humidity BIGINT, pressure DOUBLE, rain DOUBLE) 
      WITH (KAFKA_TOPIC='WEATHER_NESTED', VALUE_FORMAT='JSON');

ksql> select * from weather emit changes limit 5; 

+-----------------------------------------------------------------------+----------------+----------+------+-----------+-----------+--------+
|CITY                                                                   |DESCRIPTION     |CLOUDS    |DEG   |HUMIDITY   |PRESSURE   |RAIN    |
+-----------------------------------------------------------------------+----------------+----------+------+-----------+-------  --+--------+
|{NAME=Sydney, COUNTRY=AU, LATITUDE=-33.8688, LONGITUDE=151.2093}       |light rain       |92       |26    |94         |1025.12    |1.25    |                             
|{NAME=Seattle, COUNTRY=US, LATITUDE=47.6062, LONGITUDE=-122.33 21}     |heavy rain       |92       |19    |94         |1025.12    |7.0     |            
|{NAME=San Francisco, COUNTRY=US, LATITUDE=37.7749, LONGITUDE=-122.4194}|fog              |92       |19    |94         |1025.12    |10.0    |
|{NAME=San Jose, COUNTRY=US, LATITUDE=37.3382, LONGITUDE=-121.8863}     |light rain       |92       |23    |94         |1025.12    |3.0     |
|{NAME=Fresno, COUNTRY=US, LATITUDE=36.7378, LONGITUDE=-119.7871}       |heavy rain       |92       |22    |94         |1025.12    |6.0     |
```
To query those filed inside the struct individually we need to use the little dash Chevron to indicate the fields of the struct that we're interested in.

```sql
ksql> SELECT city->name AS city_name, city->country AS city_country, city->latitude as latitude, city->longitude as longitude, description, rain from weather emit changes; 
+-------------------+-------------------+-------------------+-------------------+-------------------+-------------------+
|CITY_NAME          |CITY_COUNTRY       |LATITUDE           |LONGITUDE          |DESCRIPTION        |RAIN               |
+-------------------+-------------------+-------------------+-------------------+-------------------+-------------------+
|Sydney             |AU                 |-33.8688           |151.2093           |light rain         |1.25               |
|Seattle            |US                 |47.6062            |-122.3321          |heavy rain         |7.0                |
|San Francisco      |US                 |37.7749            |-122.4194          |fog                |10.0               |
|San Jose           |US                 |37.3382            |-121.8863          |light rain         |3.0                |
|Fresno             |US                 |36.7378            |-119.7871          |heavy rain         |6.0                |
|Los Angeles        |US                 |34.0522            |-118.2437          |haze               |2.0                |
|San Diego          |US                 |32.7157            |-117.1611          |fog                |2.0                |
|Birmingham         |UK                 |52.4862            |-1.8904            |light rain         |4.0                |
|London             |GB                 |51.5074            |-0.1278            |heavy rain         |8.0                |
|Manchester         |GB                 |53.4808            |-2.2426            |fog                |3.0                |
|Bristol            |GB                 |51.4545            |-2.5879            |light rain         |3.0                |
|Newcastle          |GB                 |54.9783            |-1.6178            |heavy rain         |12.0               |
|Liverpool          |GB                 |53.4084            |-2.9916            |haze               |3.0                |
```