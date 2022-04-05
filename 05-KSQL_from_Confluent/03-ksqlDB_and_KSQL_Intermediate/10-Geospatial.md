# Geospatial - GEO_DISTANCE

Use GEO_DISTANCE scalar functions

```
GEO DISTANCE (lat1, lon1, lat2, lon2, unit)
```

- The distance between two points
- From and to both specified as (latitude, longitude) points
- Distance either in kilometres or miles

```sql
ksql> describe rr_world;

Name                 : RR_WORLD
 Field       | Type
-------------------------------
 DATA_SOURCE | VARCHAR(STRING)
 REQUESTTIME | BIGINT
 LATITUDE    | DOUBLE
 LONGITUDE   | DOUBLE
 RIDEID      | VARCHAR(STRING)
 USER        | VARCHAR(STRING)
 CITY_NAME   | VARCHAR(STRING)
-------------------------------
For runtime statistics and query details run: DESCRIBE <Stream,Table> EXTENDED;
```

```sql
ksql> create stream requested_journey as
select rr.latitude as from_latitude, 
rr.longitude as from_longitude, 
rr.user,
rr.city_name as city_name,
w.city_country,
w.latitude as to_latitude,
w.longitude as to_longitude,
w.description as weather_description,
w.rain
from rr_world rr
left join weather_now w on rr.city_name = w.city_name
emit changes;
 Message
--------------------------------------------------
 Created query with ID CSAS_REQUESTED_JOURNEY_137
--------------------------------------------------

select * from requested_journey emit changes;
+--------------------+--------------------+--------------------+--------------------+--------------------+--------------------+--------------------+--------------------+--------------------+
|CITY_NAME           |FROM_LATITUDE       |FROM_LONGITUDE      |USER                |CITY_COUNTRY        |TO_LATITUDE         |TO_LONGITUDE        |WEATHER_DESCRIPTION |RAIN                |
+--------------------+--------------------+--------------------+--------------------+--------------------+--------------------+--------------------+--------------------+--------------------+
|Liverpool           |52.51957735164314   |0.6138992201012576  |Heidi               |GB                  |53.4084             |-2.9916             |haze                |3.0                 |
|London              |51.22851157028174   |-0.20083747123349793|Eve                 |GB                  |51.5074             |-0.1278             |heavy rain          |8.0                 |
|London              |52.413372111531885  |-0.11087023213129044|Carol               |GB                  |51.5074             |-0.1278             |heavy rain          |8.0                 |
|Newcastle           |52.42348284943886   |-1.9302791510111423 |Carol               |GB                  |54.9783             |-1.6178             |heavy rain          |12.0                |
|London              |53.08974532263621   |-1.7776298704147004 |Ivan                |GB                  |51.5074             |-0.1278             |heavy rain          |8.0                 |
```

```sql
ksql> describe requested_journey;

Name                 : REQUESTED_JOURNEY
 Field               | Type
----------------------------------------------
 CITY_NAME           | VARCHAR(STRING)  (key)
 FROM_LATITUDE       | DOUBLE
 FROM_LONGITUDE      | DOUBLE
 USER                | VARCHAR(STRING)
 CITY_COUNTRY        | VARCHAR(STRING)
 TO_LATITUDE         | DOUBLE
 TO_LONGITUDE        | DOUBLE
 WEATHER_DESCRIPTION | VARCHAR(STRING)
 RAIN                | DOUBLE
----------------------------------------------
For runtime statistics and query details run: DESCRIBE <Stream,Table> EXTENDED;
```

```sql
ksql>select user, city_name, city_country, weather_description, rain, 
GEO_DISTANCE(from_latitude, from_longitude, to_latitude, to_longitude, 'km') as dist
from requested_journey
emit changes;

+--------------------------+--------------------------+--------------------------+--------------------------+--------------------------+--------------------------+
|USER                      |CITY_NAME                 |CITY_COUNTRY              |WEATHER_DESCRIPTION       |RAIN                      |DIST                      |
+--------------------------+--------------------------+--------------------------+--------------------------+--------------------------+--------------------------+
|Heidi                     |Liverpool                 |GB                        |haze                      |3.0                       |260.88182243878805        |
|Eve                       |London                    |GB                        |heavy rain                |8.0                       |31.42274119692008         |
|Carol                     |London                    |GB                        |heavy rain                |8.0                       |100.7461801763998         |
|Carol                     |Newcastle                 |GB                        |heavy rain                |12.0                      |284.82562389329536        |
|Ivan                      |London                    |GB                        |heavy rain                |8.0                       |208.6604523717073         |
|Carol                     |London                    |GB                        |heavy rain                |8.0                       |204.08952129765825        |
```

```sql 
ksql> create stream ridetodest as
select user, city_name, city_country, weather_description, rain, 
GEO_DISTANCE(from_latitude, from_longitude, to_latitude, to_longitude, 'km') as dist
from requested_journey
emit changes; 

 Message
-------------------------------------------
 Created query with ID CSAS_RIDETODEST_143
-------------------------------------------
+--------------------------+--------------------------+--------------------------+--------------------------+--------------------------+--------------------------+
|CITY_NAME                 |USER                      |CITY_COUNTRY              |WEATHER_DESCRIPTION       |RAIN                      |DIST                      |
+--------------------------+--------------------------+--------------------------+--------------------------+--------------------------+--------------------------+
|Liverpool                 |Heidi                     |GB                        |haze                      |3.0                       |260.88182243878805        |
|London                    |Eve                       |GB                        |heavy rain                |8.0                       |31.42274119692008         |
|London                    |Carol                     |GB                        |heavy rain                |8.0                       |100.7461801763998         |
|Newcastle                 |Carol                     |GB                        |heavy rain                |12.0                      |284.82562389329536        |
|London                    |Ivan                      |GB                        |heavy rain                |8.0                       |208.6604523717073         |
```

```sql
ksql>select concat(user, ' is traveling ', cast(round(dist) as varchar), ' km to ', city_name, ' where the weather is reported as ', weather_description ) 
from ridetodest
emit changes; 

+--------------------------------------------------------------------------------------+
|KSQL_COL_0                                                                            |
+--------------------------------------------------------------------------------------+
|Heidi is traveling 261 km to Liverpool where the weather is reported as haze         |
|Eve is traveling 31 km to London where the weather is reported as heavy rain         |
|Carol is traveling 101 km to London where the weather is reported as heavy rain      |
|Carol is traveling 285 km to Newcastle where the weather is reported as heavy rain   |
|Ivan is traveling 209 km to London where the weather is reported as heavy rain       |
|Carol is traveling 204 km to London where the weather is reported as heavy rain      |
|Ivan is traveling 39 km to London where the weather is reported as heavy rain        |
```
