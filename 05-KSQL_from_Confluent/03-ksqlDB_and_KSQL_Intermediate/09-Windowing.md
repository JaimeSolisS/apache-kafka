# Windows in KSQL

- Streaming has time based records
- In KSQL, time boundaries are named windows
- Windows can apply to streams and tables
- There are 3 ways to define time windows in KSQL
   - Tumbling - Fixed-duration time window, with no overlaps
   - Hopping - Fixed-duration, overlapping time window
   - Session - Not fixed, rather based on durations of activity data
    separated by gaps of inactivity.

## Tumbling Window
- Fixed-duration time window (e.g., 3 minutes)
- No overlaps

```sql
ksql> select data_source, city_name, count(*)
from rr_world
window tumbling (size 60 seconds)
group by data_source, city_name
emit changes;

+-------------------------------+-------------------------------+-------------------------------+
|DATA_SOURCE                    |CITY_NAME                      |KSQL_COL_0                     |
+-------------------------------+-------------------------------+-------------------------------+
|Europe                         |Birmingham                     |1                              |
|America                        |San Francisco                  |1                              |
|Europe                         |Newcastle                      |1                              |
|America                        |Seattle                        |1                              |
|Europe                         |London                         |1                              |
|America                        |San Francisco                  |2                              |
|Europe                         |London                         |2                              |
|America                        |Fresno                         |1                              |
|Europe                         |Bristol                        |1                              |
|America                        |San Francisco                  |3                              |
|Europe                         |London                         |3                              |
|America                        |San Francisco                  |4                              |
|Europe                         |Newcastle                      |2                              |
|America                        |Fresno                         |2                              |
^CQuery terminated
```

```sql
ksql>select data_source, city_name, COLLECT_LIST(user)
from rr_world
window tumbling (size 60 seconds)
group by data_source, city_name
emit changes;

+-------------------------------+-------------------------------+-------------------------------+
|DATA_SOURCE                    |CITY_NAME                      |KSQL_COL_0                     |
+-------------------------------+-------------------------------+-------------------------------+
|Europe                         |Birmingham                     |[Heidi]                        |
|America                        |Seattle                        |[Judy]                         |
|Europe                         |London                         |[Frank]                        |
|America                        |Seattle                        |[Wendy]                        |
|Europe                         |Birmingham                     |[Eve]                          |
|America                        |San Francisco                  |[Judy]                         |
|Europe                         |London                         |[Frank]                        |
|America                        |Fresno                         |[Sybil]                        |
|Europe                         |Birmingham                     |[Eve, Grace]                   |
|America                        |Fresno                         |[Sybil, Mike]                  |
|Europe                         |London                         |[Frank, Carol]                 |
|America                        |San Francisco                  |[Judy, Ted]                    |
|Europe                         |London                         |[Frank, Carol, Heidi]          |
|America                        |Los Angeles                    |[Oscar]                        |
^CQuery terminated
```

```sql
ksql> select TIMESTAMPTOSTRING(Windowstart, 'HH:mm:ss'), TIMESTAMPTOSTRING(WindowEnd, 'HH:mm:ss'), data_source, 
TOPK (city_name, 3), count(*)
FROM rr_world
WINDOW TUMBLING (SIZE 1 minute)
GROUP BY data_source
emit changes; 

+-----------------------------+-----------------------------+-----------------------------+-----------------------------+-----------------------------+
|KSQL_COL_0                   |KSQL_COL_1                   |DATA_SOURCE                  |KSQL_COL_2                   |KSQL_COL_3                   |
+-----------------------------+-----------------------------+-----------------------------+-----------------------------+-----------------------------+
|00:18:00                     |00:19:00                     |Europe                       |[Bristol]                    |1                            |
|00:18:00                     |00:19:00                     |America                      |[San Francisco]              |1                            |
|00:18:00                     |00:19:00                     |Europe                       |[London, Bristol]            |2                            |
|00:18:00                     |00:19:00                     |America                      |[San Francisco, San Francisco|2                            |
|                             |                             |                             |]                            |                             |
|00:18:00                     |00:19:00                     |Europe                       |[London, Liverpool, Bristol] |3                            |
|00:18:00                     |00:19:00                     |America                      |[San Francisco, San Francisco|3                            |
|                             |                             |                             |, Fresno]                    |                             |
|00:18:00                     |00:19:00                     |Europe                       |[Manchester, London, Liverpoo|4                            |
|                             |                             |                             |l]                           |                             |
|00:18:00                     |00:19:00                     |America                      |[Seattle, San Francisco, San |4                            |
|                             |                             |                             |Francisco]                   |                             |
```


## Hopping Window
- Fixed-duration time window (e.g., 3 minutes)
- A "hop" interval (e.g. 1 minute)
- Overlaps

## Session Window 
- No fixed duration
- Windows based on durations of activity
- Windows separated by gaps of inactivity

## Within our windows
 - We can aggregate - e.g., count(*)
 - We can group
  - Worth knowing
    - COLLECT_LIST
    - TOPK
    - WindowStart() and WindowEnd()

