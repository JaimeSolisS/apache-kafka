# Running WordCount Application:

Launch Zookeper
```cmd
zookeeper-server-start.bat %KAFKA%\config\zookeeper.properties
```
Start Kafka Broker
```cmd
kafka-server-start.bat %KAFKA%/config/server-0.properties
```
Create input topic
```cmd
kafka-topics.bat --bootstrap-server localhost:9092 --topic word-count-input --create --partitions 2 --replication-factor 1
```
Create Output Topic
```cmd
kafka-topics.bat --bootstrap-server localhost:9092 --topic word-count-output --create --partitions 2 --replication-factor 1
```
Start Consumer
```cmd
kafka-console-consumer.bat ^
    --bootstrap-server localhost:9092 ^
    --topic word-count-output ^
    --from-beginning ^
    --formatter kafka.tools.DefaultMessageFormatter ^
    --property print.key=true ^
    --property print.value=true ^
    --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer ^
    --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer 
``` 
Start Application. You should see this in your log
```
INFO StreamsConfig values: 
	acceptable.recovery.lag = 10000
	application.id = wordcount-application
...
INFO stream-thread [wordcount-application-ab9f42a1-1492-479f-be20-f73ac541fc01-StreamThread-1] Creating consumer client (org.apache.kafka.streams.processor.internals.StreamThread:386) 
INFO ConsumerConfig values: 
    ...
	group.id = wordcount-application
...
INFO stream-client [wordcount-application-ab9f42a1-1492-479f-be20-f73ac541fc01] State transition from CREATED to REBALANCING (org.apache.kafka.streams.KafkaStreams:314) 
...
INFO stream-client [wordcount-application-ab9f42a1-1492-479f-be20-f73ac541fc01] State transition from REBALANCING to RUNNING (org.apache.kafka.streams.KafkaStreams:314) 
```
You we'll see all the log, as long as there are no ERRORS or WARNINGS, everything is fine. 

Start a producer
```cmd
kafka-console-producer.bat --broker-list localhost:9092 --topic streams-plaintext-input
hello kafka streams
Kafka streams is working
Kafka is awesome
```
 We won't get anything new in app but in consumer console we'll see that data has been written to the topic
 ```
hello	1
working	1
kafka	2
streams	2
is	1
kafka	3
is	2
awesome	1
 ```

Of we stop the application, it should shutdown gracefully and we'll see the following logs
```
INFO stream-client [wordcount-application-ab9f42a1-1492-479f-be20-f73ac541fc01] State transition from RUNNING to PENDING_SHUTDOWN (org.apache.kafka.streams.KafkaStreams:314) 
...
INFO stream-thread [wordcount-application-ab9f42a1-1492-479f-be20-f73ac541fc01-StreamThread-1] State transition from PENDING_SHUTDOWN to DEAD (org.apache.kafka.streams.processor.internals.StreamThread:229) 
...
INFO stream-client [wordcount-application-ab9f42a1-1492-479f-be20-f73ac541fc01] State transition from PENDING_SHUTDOWN to NOT_RUNNING (org.apache.kafka.streams.KafkaStreams:314) 
```