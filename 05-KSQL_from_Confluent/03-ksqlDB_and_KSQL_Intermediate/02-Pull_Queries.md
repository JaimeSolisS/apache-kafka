# Push and Pull Queries

Push queries need `EMIT CHANGES`
- Push queries – constantly query & output results
- Continue to output results until
    - terminated by the user
    - exceed LIMIT condition
- Push queries were the default in KSQL 5.3 and earlier

## Pull queries

- Pull queries - the current state of the system
-  Return a result, and terminates
- New from 5.4
- KSQL currently only supports pull queries on aggregate tables
    ```sql
    create table countryDrivers as
    select countrycode, count (*)
    from driverLocations
    group by countrycode
    ```
- Must query against rowkey
    ```sql
    select countrycode, numdrivers
    from countryDrivers
    where rowkey='AU';
    ```

    ### Create Stream and populate it

```sql
CREATE STREAM driverLocations (driverId INTEGER key, countrycode VARCHAR, city VARCHAR, driverName VARCHAR)WITH (kafka_topic='driverlocations', value_format='json', partitions=1); 

 Message
----------------
 Stream created
----------------

INSERT INTO driverLocations (driverId, countrycode, city, driverName) VALUES (1, 'AU', 'Sydney', 'Alice'); 
INSERT INTO driverLocations (driverId, countrycode, city, driverName) VALUES (2, 'AU', 'Melbourne', 'Bob'); 
INSERT INTO driverLocations (driverId, countrycode, city, driverName) VALUES (3, 'GB', 'London', 'Carole'); 
INSERT INTO driverLocations (driverId, countrycode, city, driverName) VALUES (4, 'US', 'New York', 'Derek'); 

ksql> select * from driverLocations emit changes;
+------------------+------------------+------------------+------------------+
|DRIVERID          |COUNTRYCODE       |CITY              |DRIVERNAME        |
+------------------+------------------+------------------+------------------+
|1                 |AU                |Sydney            |Alice             |
|2                 |AU                |Melbourne         |Bob               |
|3                 |GB                |London            |Carole            |
|4                 |US                |New York          |Derek             |
```
### Create an Aggregate table against this stream

```sql
create table countryDrivers as
select countrycode, count(*) as numDrivers
from driverLocations
group by countrycode; 

 Message
----------------------------------------------
 Created query with ID CTAS_COUNTRYDRIVERS_37
----------------------------------------------
```
### Query table
```sql
ksql> select * from countryDrivers where countrycode='AU';
+---------------------------------------+---------------------------------------+
|COUNTRYCODE                            |NUMDRIVERS                             |
+---------------------------------------+---------------------------------------+
|AU                                     |2                                      |
```
### Insert a new record and query again
```sql
ksql> INSERT INTO driverLocations (driverId, countrycode, city, driverName) VALUES (5, 'AU', 'Sydney', 'Emma');
ksql> select * from countryDrivers where countrycode='AU';
+---------------------------------------+---------------------------------------+
|COUNTRYCODE                            |NUMDRIVERS                             |
+---------------------------------------+---------------------------------------+
|AU                                     |3                                      |
Query terminated
```