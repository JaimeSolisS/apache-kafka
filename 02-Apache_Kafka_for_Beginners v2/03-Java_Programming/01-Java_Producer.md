# Kafka Java Programming 101

We'll write code in Java. Using Java we'll be able to reproduce what the CLI does and even more. In this section we will write a producer, we will write a consumer, and learn all about the quirks of the APIs and tips and tricks.

# Creating Kafka Project

Create new Maven Project and add the following dependencies:

```xml
<!-- https://mvnrepository.com/artifact/org.apache.kafka/kafka-clients -->
<dependency>
    <groupId>org.apache.kafka</groupId>
    <artifactId>kafka-clients</artifactId>
    <version>2.8.0</version>
</dependency>

<!-- https://mvnrepository.com/artifact/org.slf4j/slf4j-simple -->
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-simple</artifactId>
    <version>1.7.25</version>
</dependency>
```
# Java Producer  

There are four steps to create a producer:  
1. Create producer properties  
2. Create the Producer 
3. Create Producer Record 
4. Send Data

```java
String bootstrapServers = "localhost:9092";
// create Producer properties
Properties properties = new Properties ();
properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName ());
properties.setProperty(ProducerConfig. VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName ());

// create the producer
KafkaProducer<String, String> producer = new KafkaProducer<String, String>(properties);

// create producer record
ProducerRecord<String, String> record = new ProducerRecord<>("first_topic", "hello world");

// send data - asynchronous
producer.send(record);

//flush data
producer.flush();
// flush and close producer
producer.close();
```
`key.serializer` and `value.serializer` basically help the producer know what type of value you're sending to kafka, and how this should be serialized to bytes. Because the kafka client will convert whatever we send, to kafka into bytes.

When you create a producer and you send data, this is asynchronous. And so, this is going to happen in the background. We need to use producer.flush() to wait for the data to be produced. Or, alternatively because it sends the end of the program, we do producer.close().

# Java Producer Callbacks

wouldn't it be nice if we had the option to understand where the message was produced and understand if it was produced correctly and what's the offset value and the partition number, etc?

So using a copy of last example we are going to add a callback inside the new class. Everything else remains the same. 

```java
producer.send (record, new Callback() {
    public void onCompletion (RecordMetadata recordMetadata, Exception e) {
        // executes every time a record is successfully sent or an exception is thrown
        if (e == null) {
            // the record was successfully sent
            logger.info("Received new metadata. \n" +
                    "Topic:" + recordMetadata.topic() + "\n" +
                    "Partition: " + recordMetadata.partition() + "\n" +
                    "Offset: " + recordMetadata.offset() + "\n" +
                    "Timestamp: " + recordMetadata.timestamp());
        } else {
            logger.error("Error while producing", e);
        }
    }
});
```
onCompletion function executes every time we get a record being sent or there is an exception. Well, we can look at ther `recordMetadata` object. So record metadata has a bunch of functions attached to it. It has .offsets, .partition, .topic, .timestamp and more so we can start logging things out. 

We wrap all the code into a for-loop and we can run the code. 

```java
for (int i=0; i<10; i++ ) {
            // create a producer record
            ProducerRecord<String, String> record = new ProducerRecord<>("first_topic",  "hello world " + Integer.toString(i));
            // send data - asynchronous
            producer.send (record, new Callback() {
                public void onCompletion (RecordMetadata recordMetadata, Exception e) {
                    // executes every time a record is successfully sent or an exception is thrown
                    if (e == null) {
                        // the record was successfully sent
                        logger.info("Received new metadata. \n" +
                                "Topic:" + recordMetadata.topic() + "\n" +
                                "Partition: " + recordMetadata.partition() + "\n" +
                                "Offset: " + recordMetadata.offset() + "\n" +
                                "Timestamp: " + recordMetadata.timestamp());
                    } else {
                        logger.error("Error while producing", e);
                    }
                }
            });
}
```

##  Note
You may see that all messages are going into the same partition, if you are using kafka >= 2.4.0 that is ok, since you are using [StickyPartitioner](https://www.confluent.io/blog/apache-kafka-producer-improvements-sticky-partitioner/).  

- If you add a Thread.sleep(1000) to wait a second before sending the next message, you would get a round robin distribution. Otherwise, Kafka uses sticky partitioning strategy and sends the bunch of messages to a single partition.

- Also to work around the sticky partitioning strategy you can just set the batch.size to a very small value (it's meant to be in byte). So by adding `producerProperties.setProperty(ProducerConfig.BATCH_SIZE_CONFIG,"1");` to your properties it should work again as expected.

- Or simply move the flush() to the loop and flush data in each iteration. 

## Producer Results 
```
[kafka-producer-network-thread | producer-1] INFO com.jsolis.kafka.t01.ProducerDemoWithCallback - Received new metadata. 
Topic:first_topic
Partition: 1
Offset: 0
Timestamp: 1646684569510
[kafka-producer-network-thread | producer-1] INFO com.jsolis.kafka.t01.ProducerDemoWithCallback - Received new metadata. 
Topic:first_topic
Partition: 0
Offset: 0
Timestamp: 1646684569549
[kafka-producer-network-thread | producer-1] INFO com.jsolis.kafka.t01.ProducerDemoWithCallback - Received new metadata. 
Topic:first_topic
Partition: 2
Offset: 0
Timestamp: 1646684569552
[kafka-producer-network-thread | producer-1] INFO com.jsolis.kafka.t01.ProducerDemoWithCallback - Received new metadata. 
Topic:first_topic
Partition: 0
Offset: 1
Timestamp: 1646684569554
[kafka-producer-network-thread | producer-1] INFO com.jsolis.kafka.t01.ProducerDemoWithCallback - Received new metadata. 
Topic:first_topic
Partition: 1
Offset: 1
Timestamp: 1646684569556
[kafka-producer-network-thread | producer-1] INFO com.jsolis.kafka.t01.ProducerDemoWithCallback - Received new metadata. 
Topic:first_topic
Partition: 0
Offset: 2
Timestamp: 1646684569559
[kafka-producer-network-thread | producer-1] INFO com.jsolis.kafka.t01.ProducerDemoWithCallback - Received new metadata. 
Topic:first_topic
Partition: 2
Offset: 1
Timestamp: 1646684569561
[kafka-producer-network-thread | producer-1] INFO com.jsolis.kafka.t01.ProducerDemoWithCallback - Received new metadata. 
Topic:first_topic
Partition: 1
Offset: 2
Timestamp: 1646684569563
[kafka-producer-network-thread | producer-1] INFO com.jsolis.kafka.t01.ProducerDemoWithCallback - Received new metadata. 
Topic:first_topic
Partition: 0
Offset: 3
Timestamp: 1646684569564
[kafka-producer-network-thread | producer-1] INFO com.jsolis.kafka.t01.ProducerDemoWithCallback - Received new metadata. 
Topic:first_topic
Partition: 1
Offset: 3
Timestamp: 1646684569566
```
## Consumer Results

```
hello world 0
hello world 1
hello world 3
hello world 5
hello world 8
hello world 4
hello world 7
hello world 9
hello world 2
hello world 6
```

# Java Producer with Key
Copy last class and change name to ProducerDemoWithKeys and add the key to record object. Everything else remains withouth changes. 

```java
String topic = "first_topic";
String value = "Hello World " + Integer.toString(i);
String key = "id_" + Integer.toString(i);
// create a producer record
ProducerRecord<String, String> record = new ProducerRecord<>(topic, key,  value);

logger.info("Key: " + key); // log the key
```

If we run the code once we'll see (partial output for simplicity):

| First Run                | Second Run              |
|------------------------- |-------------------------|
| Key: id_0  Partition: 1  | Key: id_0 Partition: 1  |
| Key: id_1 Partition: 0   | Key: id_1 Partition: 0  |
| Key: id_2 Partition: 2   | Key: id_2 Partition: 2  |
| Key: id_3 Partition: 0   | Key: id_3 Partition: 0  |
| Key: id_4 Partition: 2   | Key: id_4 Partition: 2  |
| Key: id_5 Partition: 2   | Key: id_5 Partition: 2  |
| Key: id_6 Partition: 0   | Key: id_6 Partition: 0  |
| Key: id_7 Partition: 2   | Key: id_7 Partition: 2  |
| Key: id_8 Partition: 1   | Key: id_8 Partition: 1  |
| Key: id_9 Partition: 2   | Key: id_9 Partition: 2  |

So basically by providing a key we guarantee that the same key always goes to the same partition.