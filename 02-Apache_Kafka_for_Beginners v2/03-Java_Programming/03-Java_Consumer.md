# Kafka Java Programming 101

Okay, so now that we have a producer working and several demos working, we are going to create a new Java class, and this one is going to be called ConsumerDemo.
# Java Consumer 

There are four steps to create a consumer:  
1. Create consumer properties  
2. Create the Consumer 
3. Subscribe consumer to topic
4. Poll for Data

```java 
String bootstrapServers = "localhost:9092";
String groupId = "my-first-application";
String topic = "first_topic";

// create consumer configs
Properties properties = new Properties ();
properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName ( ));
properties.setProperty(ConsumerConfig. VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName ());
properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); //earliest, latest, none

// create consumer
KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(properties);

// subscribe consumer to our topic(s)
consumer.subscribe(Arrays.asList(topic));

// poll for new data
while(true){
    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100)); // new in Kafka 2.0.0
    // iterate over each record
    for (ConsumerRecord<String, String> record : records){
        logger.info("Key: " +record.key() + ", Value: " + record.value());
        logger.info("Partition: " + record.partition() + ", Offset:" + record.offset());
    }
}
```
The consumer does not get the data until it asks for that data. 

## Results
```
[main] INFO com.jsolis.kafka.t01.ConsumerDemo - Key: id_1, Value: Hello World 1
[main] INFO com.jsolis.kafka.t01.ConsumerDemo - Partition: 0, Offset:0
[main] INFO com.jsolis.kafka.t01.ConsumerDemo - Key: id_3, Value: Hello World 3
[main] INFO com.jsolis.kafka.t01.ConsumerDemo - Partition: 0, Offset:1
[main] INFO com.jsolis.kafka.t01.ConsumerDemo - Key: id_6, Value: Hello World 6
[main] INFO com.jsolis.kafka.t01.ConsumerDemo - Partition: 0, Offset:2
[main] INFO com.jsolis.kafka.t01.ConsumerDemo - Key: id_0, Value: Hello World 0
[main] INFO com.jsolis.kafka.t01.ConsumerDemo - Partition: 1, Offset:0
[main] INFO com.jsolis.kafka.t01.ConsumerDemo - Key: id_8, Value: Hello World 8
[main] INFO com.jsolis.kafka.t01.ConsumerDemo - Partition: 1, Offset:1
[main] INFO com.jsolis.kafka.t01.ConsumerDemo - Key: id_2, Value: Hello World 2
[main] INFO com.jsolis.kafka.t01.ConsumerDemo - Partition: 2, Offset:0
[main] INFO com.jsolis.kafka.t01.ConsumerDemo - Key: id_4, Value: Hello World 4
[main] INFO com.jsolis.kafka.t01.ConsumerDemo - Partition: 2, Offset:1
[main] INFO com.jsolis.kafka.t01.ConsumerDemo - Key: id_5, Value: Hello World 5
[main] INFO com.jsolis.kafka.t01.ConsumerDemo - Partition: 2, Offset:2
[main] INFO com.jsolis.kafka.t01.ConsumerDemo - Key: id_7, Value: Hello World 7
[main] INFO com.jsolis.kafka.t01.ConsumerDemo - Partition: 2, Offset:3
[main] INFO com.jsolis.kafka.t01.ConsumerDemo - Key: id_9, Value: Hello World 9
[main] INFO com.jsolis.kafka.t01.ConsumerDemo - Partition: 2, Offset:4



[main] INFO com.jsolis.kafka.t01.ConsumerDemo - Key: id_0, Value: Hello World 0
[main] INFO com.jsolis.kafka.t01.ConsumerDemo - Partition: 1, Offset:2
[main] INFO com.jsolis.kafka.t01.ConsumerDemo - Key: id_1, Value: Hello World 1
[main] INFO com.jsolis.kafka.t01.ConsumerDemo - Partition: 0, Offset:3
[main] INFO com.jsolis.kafka.t01.ConsumerDemo - Key: id_3, Value: Hello World 3
[main] INFO com.jsolis.kafka.t01.ConsumerDemo - Partition: 0, Offset:4
[main] INFO com.jsolis.kafka.t01.ConsumerDemo - Key: id_6, Value: Hello World 6
[main] INFO com.jsolis.kafka.t01.ConsumerDemo - Partition: 0, Offset:5
[main] INFO com.jsolis.kafka.t01.ConsumerDemo - Key: id_8, Value: Hello World 8
[main] INFO com.jsolis.kafka.t01.ConsumerDemo - Partition: 1, Offset:3
[main] INFO com.jsolis.kafka.t01.ConsumerDemo - Key: id_2, Value: Hello World 2
[main] INFO com.jsolis.kafka.t01.ConsumerDemo - Partition: 2, Offset:5
[main] INFO com.jsolis.kafka.t01.ConsumerDemo - Key: id_4, Value: Hello World 4
[main] INFO com.jsolis.kafka.t01.ConsumerDemo - Partition: 2, Offset:6
[main] INFO com.jsolis.kafka.t01.ConsumerDemo - Key: id_5, Value: Hello World 5
[main] INFO com.jsolis.kafka.t01.ConsumerDemo - Partition: 2, Offset:7
[main] INFO com.jsolis.kafka.t01.ConsumerDemo - Key: id_7, Value: Hello World 7
[main] INFO com.jsolis.kafka.t01.ConsumerDemo - Partition: 2, Offset:8
[main] INFO com.jsolis.kafka.t01.ConsumerDemo - Key: id_9, Value: Hello World 9
[main] INFO com.jsolis.kafka.t01.ConsumerDemo - Partition: 2, Offset:9
```
As you could see, in the first batch, the consumer was launched after the producer was executed, it first read all the values from partition zero, all the values from partition zero, then it went on to partition one, so it read all the values from partition one, then it went on to partition two and got all the values from partition two. 

The second batch the producer was executed while the consumer was running and we can see that it is reading the messages as they arrive to the consumer. 

# Java Consumer inside Consumer Group
Duplicate last file and create `ConsumerDemoGroups` class. 

If we run this code again, it will not show me anything. That's because our Consumer Group has already been started and it's been reading all the way up until the end. So obviously, if we wanted to restart the application to read from the beginning, we either have to reset the groupID or as we've done before

## Rebalancing 

So with the new consumer running we'll run another consumer. If we look at the log, there has been some log being created. So if we go there is Abstract Coordinatorb and a Consumer Coordinator and we go all the way to the right side, and it says
```log
Attempt to heartbeat failed since group is rebalancing
Revoke previously assigned partitions first_topic-0, first_topic-1, first_topic-2
(Re-)joining group
...
Successfully joined group...
{consumer-my-second-application-1-75bb7fa6-4240-4aa8-be03-dbf874a13e8e=Assignment(partitions=[first_topic-2]),   consumer-my-second-application-1-22865c72-98bc-4f4b-a356-4fbe4c7eb959=Assignment(partitions=[first_topic-0, first_topic-1])}
...
```

So because we've started a second consumer, the group started rebalancing. So previously, assigned partitions for the first consumer was partition 0, partition 1, and partition 2 but because it rejoined the group, it has a newly assigned partitions, which is just partition 2, while the second consumer has assigned partitions 0 and 1. 
If we run the producer once again, we'll see that consumer one will read only partition 2 while the other will read from 0 and 1. 

## Results 
Consumer 1:
```
[main] INFO com.jsolis.kafka.t01.ConsumerDemoGroups - Key: id_2, Value: Hello World 2
[main] INFO com.jsolis.kafka.t01.ConsumerDemoGroups - Partition: 2, Offset:5
[main] INFO com.jsolis.kafka.t01.ConsumerDemoGroups - Key: id_4, Value: Hello World 4
[main] INFO com.jsolis.kafka.t01.ConsumerDemoGroups - Partition: 2, Offset:6
[main] INFO com.jsolis.kafka.t01.ConsumerDemoGroups - Key: id_5, Value: Hello World 5
[main] INFO com.jsolis.kafka.t01.ConsumerDemoGroups - Partition: 2, Offset:7
[main] INFO com.jsolis.kafka.t01.ConsumerDemoGroups - Key: id_7, Value: Hello World 7
[main] INFO com.jsolis.kafka.t01.ConsumerDemoGroups - Partition: 2, Offset:8
[main] INFO com.jsolis.kafka.t01.ConsumerDemoGroups - Key: id_9, Value: Hello World 9
[main] INFO com.jsolis.kafka.t01.ConsumerDemoGroups - Partition: 2, Offset:9
```

Consumer 2: 
```
[main] INFO com.jsolis.kafka.t01.ConsumerDemoGroups - Key: id_0, Value: Hello World 0
[main] INFO com.jsolis.kafka.t01.ConsumerDemoGroups - Partition: 1, Offset:2
[main] INFO com.jsolis.kafka.t01.ConsumerDemoGroups - Key: id_1, Value: Hello World 1
[main] INFO com.jsolis.kafka.t01.ConsumerDemoGroups - Partition: 0, Offset:3
[main] INFO com.jsolis.kafka.t01.ConsumerDemoGroups - Key: id_3, Value: Hello World 3
[main] INFO com.jsolis.kafka.t01.ConsumerDemoGroups - Partition: 0, Offset:4
[main] INFO com.jsolis.kafka.t01.ConsumerDemoGroups - Key: id_6, Value: Hello World 6
[main] INFO com.jsolis.kafka.t01.ConsumerDemoGroups - Partition: 0, Offset:5
[main] INFO com.jsolis.kafka.t01.ConsumerDemoGroups - Key: id_8, Value: Hello World 8
[main] INFO com.jsolis.kafka.t01.ConsumerDemoGroups - Partition: 1, Offset:3
```

If we start a third consumer, it will revoke the previously assign partitions in the group and after the rebalance each consumer will read from 1 partition only. 
If we stop a consumer, the consumer will detect this and will rebalance again. 

# Assign and Seek 

Assign and seek is basically another way of writing an application. We delete the group ID and we don't subscribe to topics.

```java
// assign and seek are mostly used to replay data or fetch a specific message

// assign specific topic partition
TopicPartition partitionToReadFrom = new TopicPartition(topic, 0);
long offsetToReadFrom = 15L;
consumer.assign(Arrays.asList(partitionToReadFrom));

// seek specific offset
consumer.seek(partitionToReadFrom, offsetToReadFrom);

// how many messages we want to read
int numberOfMessagesToRead = 5;
boolean keepOnReading = true;
int numberOfMessagesReadSoFar = 0;

// poll for new data
while(keepOnReading){
    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100)); // new in Kafka 2.0.0

    for (ConsumerRecord<String, String> record : records){
        numberOfMessagesReadSoFar += 1;
        logger.info("Key: " + record.key() + ", Value: " + record.value());
        logger.info("Partition: " + record.partition() + ", Offset:" + record.offset());
        if (numberOfMessagesReadSoFar >= numberOfMessagesToRead){
            keepOnReading = false; // to exit the while loop
            break; // to exit the for loop
        }
    }
}
logger.info("Exiting the application");
```
## Results 

```
[main] INFO com.jsolis.kafka.t01.ConsumerDemoAssignSeek - Key: id_1, Value: Hello World 1
[main] INFO com.jsolis.kafka.t01.ConsumerDemoAssignSeek - Partition: 0, Offset:15
[main] INFO com.jsolis.kafka.t01.ConsumerDemoAssignSeek - Key: id_3, Value: Hello World 3
[main] INFO com.jsolis.kafka.t01.ConsumerDemoAssignSeek - Partition: 0, Offset:16
[main] INFO com.jsolis.kafka.t01.ConsumerDemoAssignSeek - Key: id_6, Value: Hello World 6
[main] INFO com.jsolis.kafka.t01.ConsumerDemoAssignSeek - Partition: 0, Offset:17
[main] INFO com.jsolis.kafka.t01.ConsumerDemoAssignSeek - Key: id_1, Value: Hello World 1
[main] INFO com.jsolis.kafka.t01.ConsumerDemoAssignSeek - Partition: 0, Offset:18
[main] INFO com.jsolis.kafka.t01.ConsumerDemoAssignSeek - Key: id_3, Value: Hello World 3
[main] INFO com.jsolis.kafka.t01.ConsumerDemoAssignSeek - Partition: 0, Offset:19
[main] INFO com.jsolis.kafka.t01.ConsumerDemoAssignSeek - Exiting the application
```
As we can see, we start at partition zero, offset 15, just like we intended. We read five messages and then we exit the application. So that's it, that's just a method for you to replay a bit of data, it's a less-used API. The API you're mostly going to use is the one we saw before using subscribe and a group ID configuration.
