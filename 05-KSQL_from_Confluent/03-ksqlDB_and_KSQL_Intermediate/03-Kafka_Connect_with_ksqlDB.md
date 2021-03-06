# Kafka Connect with ksqlDB

The course uses Postgress but the container provided has SQL Server so we're going to try use it instead of adding Postgress and recreating the cointainer. 

## Initialize and hydrate database with records

```sql
$ ls
bin  boot  dev  etc  home  lib  lib32  lib64  libx32  media  mnt  opt  proc  root  run  sbin  srv  sys  tmp  usr  var
$ /opt/mssql-tools/bin/sqlcmd -S localhost -U SA -P KSQLStreamsDemo4u!@@
1> select name from sys.Databases;
2> go
name                                                                                                                    
--------------------------------------------------------------------------------------------------------------------------------
master                                                                                                                  
tempdb                                                                                                                  
model                                                                                                                   
msdb                                                                                                                    

(4 rows affected)
-- CREATE DATABASE
1> create database kafka
2> go
1> select name from sys.Databases;
2> go
name                                                                                                                    
--------------------------------------------------------------------------------------------------------------------------------
master                                                                                                                  
tempdb                                                                                                                  
model                                                                                                                   
msdb                                                                                                                    
kafka                                                                                                                   

(5 rows affected)
1> use kafka
-- CREATE TABLE carusers
1> CREATE TABLE carusers (username NVARCHAR(50), ref INT IDENTITY(1,1) PRIMARY KEY);
2> go
-- INSERT RECORDS
1> INSERT INTO carusers VALUES ('Alice');
2> go

(1 rows affected)
1> INSERT INTO carusers VALUES ('Bob');
2> go

(1 rows affected)
1> INSERT INTO carusers VALUES ('Charlie');
2> go

(1 rows affected)
1> select * from carusers;
2> go
username                                           ref
-------------------------------------------------- -----------
Alice                                                        1
Bob                                                          2
Charlie                                                      3

(3 rows affected)
```

## Create Kafka Connctor Source Adapter

```sql
CREATE SOURCE CONNECTOR `mssql-jdbc-source` WITH(
    "connector.class"='io.confluent.connect.jdbc.JdbcSourceConnector', 
    "connection.url"='jdbc:sqlserver://localhost:1433;databaseName=kafka;', 
    "connection.user"='SA',
    "connection.password"='KSQLStreamsDemo4u!@@',
    "table.whitelist"='carusers',
    "mode"='incrementing',
    "incrementing.column.name"='ref',
    "topic.prefix"='db-',
    "key"='username');

Error
 {
  "error_code" : 500,
  "message" : "Failed to find any class that implements Connector and which name matches io.confluent.connect.jdbc.JdbcSourceConnector, available connectors are: 

  PluginDesc{klass=class io.confluent.kafka.connect.datagen.DatagenConnector, name='io.confluent.kafka.connect.datagen.DatagenConnector', version='null', encodedVersion=null, type=source, typeName='source', location='file:/usr/share/confluent-hub-components/confluentinc-kafka-connect-datagen/'}, 

  PluginDesc{klass=class io.debezium.connector.sqlserver.SqlServerConnector, name='io.debezium.connector.sqlserver.SqlServerConnector', version='1.7.1.Final', encodedVersion=1.7.1.Final, type=source, typeName='source', location='file:/usr/share/confluent-hub-components/debezium-debezium-connector-sqlserver/'}, 

  PluginDesc{klass=class org.apache.kafka.connect.file.FileStreamSinkConnector, name='org.apache.kafka.connect.file.FileStreamSinkConnector', version='6.2.0-ce', encodedVersion=6.2.0-ce, type=sink, typeName='sink', location='file:/usr/share/java/kafka/'}, 

  PluginDesc{klass=class org.apache.kafka.connect.file.FileStreamSourceConnector, name='org.apache.kafka.connect.file.FileStreamSourceConnector', version='6.2.0-ce', encodedVersion=6.2.0-ce, type=source, typeName='source', location='file:/usr/share/java/kafka/'}, 

  PluginDesc{klass=class org.apache.kafka.connect.mirror.MirrorCheckpointConnector, name='org.apache.kafka.connect.mirror.MirrorCheckpointConnector', version='1', encodedVersion=1, type=source, typeName='source', location='file:/usr/share/java/kafka/'},

  PluginDesc{klass=class org.apache.kafka.connect.mirror.MirrorHeartbeatConnector, name='org.apache.kafka.connect.mirror.MirrorHeartbeatConnector', version='1', encodedVersion=1, type=source, typeName='source', location='file:/usr/share/java/kafka/'},

  PluginDesc{klass=class org.apache.kafka.connect.mirror.MirrorSourceConnector, name='org.apache.kafka.connect.mirror.MirrorSourceConnector', version='1', encodedVersion=1, type=source, typeName='source', location='file:/usr/share/java/kafka/'}, 

  PluginDesc{klass=class org.apache.kafka.connect.tools.MockConnector, name='org.apache.kafka.connect.tools.MockConnector', version='6.2.0-ce', encodedVersion=6.2.0-ce, type=connector, typeName='connector', location='file:/usr/share/java/confluent-control-center/'}, 

  PluginDesc{klass=class org.apache.kafka.connect.tools.MockSinkConnector, name='org.apache.kafka.connect.tools.MockSinkConnector', version='6.2.0-ce', encodedVersion=6.2.0-ce, type=sink, typeName='sink', location='file:/usr/share/java/confluent-control-center/'},

  PluginDesc{klass=class org.apache.kafka.connect.tools.MockSourceConnector, name='org.apache.kafka.connect.tools.MockSourceConnector', version='6.2.0-ce', encodedVersion=6.2.0-ce, type=source, typeName='source', location='file:/usr/share/java/confluent-control-center/'},

  PluginDesc{klass=class org.apache.kafka.connect.tools.SchemaSourceConnector, name='org.apache.kafka.connect.tools.SchemaSourceConnector', version='6.2.0-ce', encodedVersion=6.2.0-ce, type=source, typeName='source', location='file:/usr/share/java/confluent-control-center/'}, 
  
  PluginDesc{klass=class org.apache.kafka.connect.tools.VerifiableSinkConnector, name='org.apache.kafka.connect.tools.VerifiableSinkConnector', version='6.2.0-ce', encodedVersion=6.2.0-ce, type=source, typeName='source', location='file:/usr/share/java/confluent-control-center/'}, 
  
  PluginDesc{klass=class org.apache.kafka.connect.tools.VerifiableSourceConnector, name='org.apache.kafka.connect.tools.VerifiableSourceConnector', version='6.2.0-ce', encodedVersion=6.2.0-ce, type=source, typeName='source', location='file:/usr/share/java/confluent-control-center/'}"
}
```

Ok, so it seems we need to install it. Open Kafka Connect CLI

```cmd
$ confluent-hub install confluentinc/kafka-connect-jdbc:latest
The component can be installed in any of the following Confluent Platform installations:
  1. / (installed rpm/deb package)
  2. / (where this tool is installed)
Choose one of these to continue the installation (1-2): 2
Do you want to install this into /usr/share/confluent-hub-components? (yN) y


Component's license:
Confluent Community License
https://www.confluent.io/confluent-community-license
I agree to the software license agreement (yN) y

Downloading component Kafka Connect JDBC 10.3.3, provided by Confluent, Inc. from Confluent Hub and installing into /usr/share/confluent-hub-components
Detected Worker's configs:
  1. Standard: /etc/kafka/connect-distributed.properties
  2. Standard: /etc/kafka/connect-standalone.properties
  3. Standard: /etc/schema-registry/connect-avro-distributed.properties
  4. Standard: /etc/schema-registry/connect-avro-standalone.properties
  5. Used by Connect process with PID : /etc/kafka-connect/kafka-connect.properties
Do you want to update all detected configs? (yN) y

Adding installation directory to plugin path in the following files:
  /etc/kafka/connect-distributed.properties
  /etc/kafka/connect-standalone.properties
  /etc/schema-registry/connect-avro-distributed.properties
  /etc/schema-registry/connect-avro-standalone.properties
  /etc/kafka-connect/kafka-connect.properties

Completed

```

Still, we receive the same error. So instead of trying directyl from ksql, I'll try to create it the old way. 
Create mssql-jdb-source.properties

```properties
# Basic configuration for our connector
name=mssql-jdbc-source
connector.class=io.confluent.connect.jdbc.JdbcSourceConnector
connection.url=jdbc:sqlserver://localhost:1433;databaseName=kafka;
connection.user=SA
connection.password=KSQLStreamsDemo4u!@@
table.whitelist=carusers
mode=incrementing
incrementing.column.name=ref
topic.prefix=db-
key=username
```
 Again I got errors so I had to update docker-compose file and restart the containers and do everything again. Now that the JDBC is installed, I was able to create the connector through ksql.

 ```sql 
 ksql> CREATE SOURCE CONNECTOR `mssql-jdbc-source` WITH(
>    "connector.class"='io.confluent.connect.jdbc.JdbcSourceConnector',
>    "connection.url"='jdbc:sqlserver://localhost:1433;databaseName=kafka;',
>    "connection.user"='SA',
>    "connection.password"='KSQLStreamsDemo4u!@@',
>    "table.whitelist"='carusers',
>    "mode"='incrementing',
>    "incrementing.column.name"='ref',
>    "topic.prefix"='db-',
>    "key"='username');

 Message
-------------------------------------
 Created connector mssql-jdbc-source
-------------------------------------
 ```

 But since the topic is not being created, check the logs and the connector failed

 ```
[2022-03-31 21:01:20,774] ERROR [Worker clientId=connect-1, groupId=compose-connect-group] Failed to start connector 'mssql-jdbc-source' (org.apache.kafka.connect.runtime.distributed.DistributedHerder)

org.apache.kafka.connect.errors.ConnectException: Failed to start connector: mssql-jdbc-source

Caused by: org.apache.kafka.connect.errors.ConnectException: Failed to transition connector mssql-jdbc-source to state STARTED

... 8 more

Caused by: org.apache.kafka.connect.errors.ConnectException: com.microsoft.sqlserver.jdbc.SQLServerException: The TCP/IP connection to the host localhost, port 1433 has failed. Error: "Connection refused (Connection refused). Verify the connection properties. Make sure that an instance of SQL Server is running on the host and accepting TCP/IP connections at the port. Make sure that TCP connections to the port are not blocked by a firewall.".
 ```

 Realized that hostname was localhost. When a container tries to access localhost, just like any other machine it's trying to communicate with itself. In this case, the ksql container will not have MSSQL running on it, so it was failing. So I changed the hostname for the containter and changed the connection string. Now it worked. 

```sql 
ksql> CREATE SOURCE CONNECTOR `mssql-jdbc-source` WITH(
>    "connector.class"='io.confluent.connect.jdbc.JdbcSourceConnector',
>    "connection.url"='jdbc:sqlserver://sqlserver:1433;databaseName=kafka;',
>    "connection.user"='SA',
>    "connection.password"='KSQLStreamsDemo4u!@@',
>    "table.whitelist"='carusers',
>    "mode"='incrementing',
>    "incrementing.column.name"='ref',
>    "topic.prefix"='db-',
>    "key"='username');

 Message
-------------------------------------
 Created connector mssql-jdbc-source
-------------------------------------
ksql> print 'db-carusers' from beginning;
Key format: KAFKA_STRING
Value format: AVRO or KAFKA_STRING
rowtime: 2022/03/31 21:30:37.880 Z, key: Alice, value: {"username": "Alice", "ref": 1}, partition: 0
rowtime: 2022/03/31 21:30:37.882 Z, key: Bob, value: {"username": "Bob", "ref": 2}, partition: 0
rowtime: 2022/03/31 21:30:37.882 Z, key: Charlie, value: {"username": "Charlie", "ref": 3}, partition: 0
```
The topic was automatically created and data has already been pulled into the topic. Insert more records in the database with sql and they should added to the topic instantly. 

```sql
INSERT INTO carusers VALUES ('Derek'); 
go
```

```sql
ksql> print 'db-carusers' from beginning;
Key format: KAFKA_STRING
Value format: AVRO or KAFKA_STRING
rowtime: 2022/03/31 21:30:37.880 Z, key: Alice, value: {"username": "Alice", "ref": 1}, partition: 0
rowtime: 2022/03/31 21:30:37.882 Z, key: Bob, value: {"username": "Bob", "ref": 2}, partition: 0
rowtime: 2022/03/31 21:30:37.882 Z, key: Charlie, value: {"username": "Charlie", "ref": 3}, partition: 0
rowtime: 2022/03/31 22:00:43.548 Z, key: Derek, value: {"username": "Derek", "ref": 4}, partition: 0
rowtime: 2022/03/31 22:36:49.440 Z, key: Emma, value: {"username": "Emma", "ref": 5}, partition: 0
```


