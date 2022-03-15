# Filestream Source Connector Standalone Mode
- Goal:
    - Read a file and load the content directly into Kafka. 
    - Run in a connector in standalone mode (useful for development).
- Learning 
    - Understand hot to configure a connector in standalone mode.
    - Get a first feel for Kafka Connect Standalone. 

worker.properties is how yo configure a worker in standalone mode. 
```properties
# from more information, visit: http://docs.confluent.io/3.2.0/connect/userguide.html#common-worker-configs
bootstrap.servers=127.0.0.1:9092
###### IMPORTANT ######
key.converter=org.apache.kafka.connect.json.JsonConverter
key.converter.schemas.enable=false
value.converter=org.apache.kafka.connect.json.JsonConverter
value.converter.schemas.enable=false
# we always leave the internal key to JsonConverter
internal.key.converter=org.apache.kafka.connect.json.JsonConverter
internal.key.converter.schemas.enable=false
internal.value.converter=org.apache.kafka.connect.json.JsonConverter
internal.value.converter.schemas.enable=false
###### --------- ######
# Rest API
rest.port=8086
rest.host.name=127.0.0.1
# this config is only for standalone workers
offset.storage.file.filename=standalone.offsets
offset.flush.interval.ms=10000
```

```properties
# These are standard kafka connect parameters, need for ALL connectors
name=file-stream-demo-standalone
connector.class=org.apache.kafka.connect.file.FileStreamSourceConnector
tasks.max=1

# Parameters specific for connector: https://github.com/apache/kafka/blob/trunk/connect/file/src/main/java/org/apache/kafka/connect/file/FileStreamSourceConnector.java
file=demo-file.txt
topic=demo-1-standalone
```

In the dir where is docker-compose execute 
```sh
# We start a hosted tools, mapped on our code
# Linux / Mac
docker run --rm -it -v "$(pwd)":/tutorial --net=host landoop/fast-data-dev:2.3.0 bash
# Windows Command Line:
docker run --rm -it -v %cd%:/tutorial --net=host landoop/fast-data-dev:2.3.0 bash
# Windows Powershell:
docker run --rm -it -v ${PWD}:/tutorial --net=host landoop/fast-data-dev:2.3.0 bash
```
Create topic
```sh
kafka-topics --create --topic demo-1-standalone --partitions 3 --replication-factor 1 --zookeeper 127.0.0.1:2181
```

Verify that the topic was created in the Web UI. 

We launched the kafka connector in standalone mode:
```sh
cd /tutorial/02-Kafka_Connect_Source 02-FileStream_Source_Connector_Standalone_mode/
```
Run a connector. Usage is connect-standalone worker.properties connector1.properties [connector2.properties connector3.properties]
```sh
connect-standalone worker.properties file-stream-demo-standalone.properties
```
Check the log
```sh
[2022-03-14 23:54:01,036] INFO Created connector file-stream-demo-standalone (org.apache.kafka.connect.cli.ConnectStandalone:112)
```
Open demo-file.txt, write some lines and they should appear on the UI. It should also create a standalone.offsets file. 

So we just ran a few properties (worker.properties and file properties) and automatically the data from file went straight to Kafka. We don't have to do much programming, **we only have to provide the right configuration**