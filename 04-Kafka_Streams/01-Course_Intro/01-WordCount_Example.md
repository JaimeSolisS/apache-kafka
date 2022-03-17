# Word Count

## Start Zookeper
```
zookeeper-server-start.bat %KAFKA%\config\zookeeper.properties
```

## Start Broker
```
kafka-server-start.bat %KAFKA%\config\server-0.properties
```

## Create input topic
```
kafka-topics.bat --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic streams-plaintext-input
```

## Create output topic
```
kafka-topics.bat --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic streams-wordcount-output
```

## Start a kafka producer and Write a few Sentences
```
kafka-console-producer.bat --broker-list localhost:9092 --topic streams-plaintext-input
>This is a test
>Test for Kafka Streams
>Apache Kafka Test
```
## Verify the data has been written
```
kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic streams-plaintext-input --from-beginning
```

## Start a consumer on the output topic
```
kafka-console-consumer.bat
    --bootstrap-server localhost:9092 
    --topic streams-wordcount-output 
    --from-beginning 
    --formatter kafka.tools.DefaultMessageFormatter
    --property print.key=true 
    --property print.value=true 
    --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer 
    --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer
```

## start the streams application
```
kafka-run-class.bat org.apache.kafka.streams.examples.wordcount.WordCountDemo
```
## Verify the data has been written to the output topic!
```
this    1
is      1
a       1
test    1
test    2
for     1
kafka   1
streams 1
apache  1
kafka   2
test    3
Processed a total of 11 messages
```