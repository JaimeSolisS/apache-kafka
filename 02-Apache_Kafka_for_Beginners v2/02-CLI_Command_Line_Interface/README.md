# Kafka Command Line Interface

We are going to go ahead and use a Kafka CLI, or command line interface, to start creating topics, producing data to Kafka, and retrieving data from Kafka.

> **For Windows**: Don't forget to add the extension `.bat` to commands being run.

## Start Zookeeper

```cmd
zookeeper-server-start.bat config/zookeeper.properties 
```

## Start Kafka Brooker 

```cmd
kafka-server-start.bat config/server.properties
```

## Kafka Topics 

Create, delete, describe, or change a topic.

As of Kafka 2.2, we need use `--bootstrap-server localhost:9092` in kafka-topics command instead of `--zookeeper localhost:2181` as this option is now deprecated. 

### Create topic

```cmd
kafka-topics.bat --bootstrap-server localhost:9092 --topic first_topic --create --partitions 3 --replication-factor 3
```

### List topics
```cmd 
kafka-topics.bat --bootstrap-server localhost:9092 --list
```

### Describe topic
```cmd 
kafka-topics.bat --bootstrap-server localhost:9092 --topic first_topic --describe
```

### Delete Topic
```cmd
kafka-topics.bat --bootstrap-server localhost:9092 --topic first_topic --delete
```
> **Do not delete topics on windows**.  
> Windows has a long-standing bug (KAFKA-1194) which makes kafka crash if we delete topics.
> The only way to recover from this error is to manually delete the folders in data/kafka.

## Console Producers CLI

```cmd
kafka-console-producer.bat --bootstrap-server localhost:9092 --topic first_topic
```

```cmd
kafka-console-producer.bat --bootstrap-server localhost:9092 --topic first_topic --producer-property acks=all
```
Always create a topic before hand, before producing to it, otherwise it'll get created with some defaults that are usually not good.

## Console Consumer CLI  
This tool helps to read data from Kafka topics and outputs it to standard output.  

Reads from the point it was launched and will read only the new messages:
```cmd
kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic first_topic 
```
```cmd
kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic first_topic --from-beginning
```

## Consumers in Group

```cmd
kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic first_topic --group application-name
```
If we launch one consumer per partition with the same group, each consumer will read the messages from one partition only.

## Consumer Groups CLI
### List
```cmd
kafka-consumer-groups.bat  --bootstrap-server localhost:9092 --list
```

### List
```cmd
kafka-consumer-groups.bat  --bootstrap-server localhost:9092 --describe --group group-name
```

### Resetting Offsets
```cmd
kafka-consumer-groups.bat --bootstrap-server localhost:9092 --group group-name --reset-offsets --to-earliest --execute --topic topic-name 
```
We will read all the messages from the beggining if we launch the consumer again. 

```cmd
kafka-consumer-groups.bat --bootstrap-server localhost:9092 --group group-name --reset-offsets --shift-by -2 --execute --topic topic-name 
```
Using this, you can shift forward or backwards for your kafka-consumer-groups.