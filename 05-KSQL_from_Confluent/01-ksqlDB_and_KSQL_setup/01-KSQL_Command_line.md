# KSQL Command Line 

# Kafka Commands  
## Create topic
```cmd
kafka-topics --zookeeper localhost:2181 --create --topic USERS --partitions 1 --replication-factor 1 
```

## Add data to topic

```cmd
kafka-console-producer --bootstrap-server localhost:9092 --topic USERS
>Alice,US
>Bob,GB
>Carole,AU
>Dan,US
```


# KSQL CLI 
## Launch CLI to interact with KSQL Server
```
$ ksql
OpenJDK 64-Bit Server VM warning: Option UseConcMarkSweepGC was deprecated in version 9.0 and will likely be removed in a future release.

                  ===========================================
                  =       _              _ ____  ____       =
                  =      | | _____  __ _| |  _ \| __ )      =
                  =      | |/ / __|/ _` | | | | |  _ \      =
                  =      |   <\__ \ (_| | | |_| | |_) |     =
                  =      |_|\_\___/\__, |_|____/|____/      =
                  =                   |_|                   =
                  =  Event Streaming Database purpose-built =
                  =        for stream processing apps       =
                  ===========================================

Copyright 2017-2021 Confluent Inc.

CLI v6.2.0, Server v6.2.0 located at http://localhost:8088
Server Status: RUNNING

Having trouble? Type 'help' (case-insensitive) for a rundown of how things work!
ksql>
```
## List topics

```sql
ksql> list topics;

 Kafka Topic                 | Partitions | Partition Replicas
---------------------------------------------------------------
 USERS                       | 1          | 1
 default_ksql_processing_log | 1          | 1
 docker-connect-configs      | 1          | 1
 docker-connect-offsets      | 25         | 1
 docker-connect-status       | 5          | 1
---------------------------------------------------------------
``` 
## Show contents of Topic
It will show only data that is newly arriving into the topic.  
```sql
ksql> print 'USERS'; 
```
This command never ends, so we need to interrup it. 

To show all data we can use `from beginning`.
To limit the number of results we can use `limit`
To show every x record we can use `interval` 
```
ksql> print 'USERS' from beginning; 
ksql> print 'USERS' from beginning limit 2; 
ksql> print 'USERS' from beginning interval 2 limit 2; 
```



