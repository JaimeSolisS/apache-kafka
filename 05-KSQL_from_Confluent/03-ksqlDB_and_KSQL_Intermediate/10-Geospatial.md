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
```