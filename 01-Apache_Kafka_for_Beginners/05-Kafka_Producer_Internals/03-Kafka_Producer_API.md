# Kafka Producer API - Internals 

## Kafka Producer Record

Kafka producer APIs are straightforward.

You can create a producer by setting some essential configurations and start sending messages using the send() method. There is only one restriction that you must package your message in a `ProducerRecord object`. 

```java
producer.send(new ProducerRecord<>(AppConfigs.topicName, MessageKey.value, Message.value));
```

While the send() method is a straightforward, A lot of things happen behind the scenes.

We package the message content in the ProducerRecord object with at least two mandatory arguments. Kafka `topic name`, and `message value`.

> ### Producer Record  
>   - Topic  
>  - Message Value

Kafka topic name is the destination address of the message. The message value is the main content of the message.

Other than these two mandatory arguments,

You can also specify the following optional items.

> ### Producer Record  
>  - Topic  
>> -  [Partition]  
>> - [Timestamp]  
>> - [Message Key]
>  - Message Value

The `message key` is one of the `most critical argument`,and it is used for many purposes,such as partitioning grouping, and joins. At this stage,consider it as another mandatory argument even if the API doesn't mandate it. `Target partition` and the `timestamp `are purely optional, and you may want to set these arguments rarely. 


The ProducerRecord wraps your message content with all necessary information such as topic name, Key, and timestamp. Once created, you can hand over the ProducerRecord to the KafkaProducer using the send() method.

## Producer Serializer
The Kafka producer is supposed to transmit the ProducerRecord to the Kafka Broker over the network. However, it does not immediately transfer the records. Every record goes through serialization, partitioning, and then it is kept in the buffer.

The serialization is necessary to send the data over the network. Without serializing data, you can't transmit it to a remote location.

However, Kafka does not know how to serialize your Key and value. That is why specifying a key serializer, and a value serializer serializer class is mandatory that we supply as part of the producer configuration. 

```java
props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class.getName());
props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
```

In the earlier example, we have used `IntegerSerializer` for the Key and a `StringSerializer` for the value. However, these serializers are the most elementary serializers, and they do not cover most of the use cases. In a real life scenario, your events are represented by complex Java objects. These objects must be serialized before the producer can transmit them to the Broker. The StringSerializer may not be a good help for those requirements. 

Kafka gives you an option to use a generic serialization library like Avro or Thrift. Alternatively, you have a choice to create custom serializer. In the upcoming lectures, we'll create a JSON serializer and show the processes of creating and using a custom serializers.

## Producer Partitioner

Well,every ProducerRecord includes a mandatory topic name as the destination address of the data. However, the Kafka topics are partitioned, and hence, the producer should also decide on which partition the message should be sent. There are two approaches to specify the target partition number for the message. 
- Set partition number argument in the ProducerRecord. Supply a partitioner class to determine the partition number at runtime. 
```java
props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, MyPartitioner.class.getName()); 
```
we supply a Partitioner class that implements your desired partitioning strategy and assigns a partition number to each message at runtime. You can specify a custom partitioner using the properties object. However, even creating a custom partitioner is often not needed because KafkaProducer comes with a default partitioner which is the most commonly used partitioner.

The default partitioner takes one of the two approaches to determine the destination topic partition.

- Hash Key Partitioning 

The first approach is based on the message key. When the message key exists, the default partitioner will use the hashing algorithm on the key to determining the partition number for the message. It is as simple as hashing the key to convert it into a numeric value. Then use the hash number to deduce the target partition. The hashing ensures that all the messages with the same key go to the same partition.

However, this algorithm also takes the total number of partitions as one of the inputs. So, if you increase the number of partitions in the topic, the default partitioner starts giving a different output. That means, if the partition is based on the key, then you should create a topic with enough partitions and never increase it at the later stage.

You can easily over provision the number of partitions in the topic. I mean, if you need hundred partitions, you can easily overprovision it by 25 percent and create 125 partitions. There is not much harm in taking this approach. But remember, if you do increase the number of partitions later, you may have to redistribute the existing messages.

- Round Robin Partitioning. 

When the message key is null, the default partitioner will use a round robin method to achieve an equal distribution among the available partitions. That means the first message goes to the one partition, the second one goes to another partition and the partitioner repeats it in a loop. The default partitioner is the most commonly used partitioner in most of the use cases.

However, Kafka allows you to implement your partitioning strategy by implementing a custom partitioner class. But you may not even need a custom partitioner in most of the cases.

## Message Timestamp

The ProducerRecord takes an optional timestamp field. The message timestamp is optional. However, for a real-time streaming application, the timestamp is the most critical value. For that reason, every message in Kafka is automatically time stamped even if you do not explicitly specify it.

Kafka allows you to implement one of the two types of message time a stamping mechanism. 

- The `CreateTime` is the time when the message was produced.
- The `LogAppendTime` is the time when the message was received at the Kafka broker. 

However, you cannot use both. Your application must decide between these two timestamping methods while creating the topic. Setting up a default time a stamping method for a topic is straightforward. You can set the `message.timestamp.type` topic configuration to 0 for using CreateTime, or you can set it to 1 for using LogAppendTime. The default value is zero.

The producer API automatically sets the current producer time to the ProducerRecord timestamp field. However, you can override the auto time stamping by explicitly specifying this argument. So, the message is transmitted with the producer time, either automatically set by the producer, or explicitly set by the developer.

When using LogAppendTime configuration, the broker will override the producer timestamp with its current local time before appending the message to the log. In this case, the producer time is overwritten by the broker time. However, the message will always have a timestamp, either a producer time or the broker time.

I prefer using default configuration as CreateTime whenever I'm using, Producer API to bring data to Kafka because the producer API automatically assigns a timestamp. However, when I'm using some other tool to bring data into Kafka, I need to understand how the tool handles the time stamping. A safer method is to configure the topic for LogAppend Time. So even if your tool is not setting a time, at least the Broker will set some value.

## Message Buffer

Once serialized and assigned a target partition number, the message goes to sit in the buffer waiting to be transmitted. The producer object consists of a partition-wise buffer space that holds the records that haven't yet been sent to the server.

The Producer also runs a background I/O thread that is responsible for turning these records into requests and transferring them to the cluster.

Why do we have this buffering? The buffering of the message is designed to offer two advantages. 
`Asynchronous send API`, and `Network Roundtrip Optimization`.

Buffering arrangement makes the send method asynchronous. That means the sender method will add the messages to the buffer and return without blocking. Those records are then transmitted by the background thread. This arrangement is quite convincing as your send() method is not delayed for the network operation. Buffering also allows the background I/O thread to combine multiple messages from the same buffer and transmit them together as a single packet to achieve better throughput.

But there is a critical consideration here. If the records are posted faster than they can be transmitted to the server, then this buffer space will be exhausted, and your next send method will block for few milliseconds until the buffer is freed by the I/O thead. 

If the I/O thread takes too long to release the buffer, then your send method throws a TimeoutException. When you are seeing such timeout exceptions, you may want to increase the producer memory. The default producer memory size is 32MB. You can expand the total memory allocated for the buffer by setting, `buffer.memory` Producer configuration. 

## Producer I/O Thread and Retires

The producer background I/O thread is responsible for transmitting the serialized messages that,are waiting in the topic partition buffer. When the broker receives the message, it sends back an acknowledgment. If the message is successfully written to Kafka, the broker will return a success acknowledgement. If the broker failed to write the message, it would return an error.

When the background I/O thread receives an error or does not receive an acknowledgment, it may retry sending the message a few more times before giving up and throwing back an error. You can control the number of retries by setting the retries producer configuration. When all the retries are failed, the I/O thread will return the error to the send() method. 

## Summary 

- We use the producer.send() method to handover the `ProducerRecord` to the `KafkaProducer object`.

- The KafkaProducer object will internally `serialize the message key and the message value`. And we provide the serializers using the properties object.

- Then the producer will determine the `target partition` number for the message to be delivered. You can provide a custom partitioner class using the properties object or provide a key and let the producer use the default partitioner.

- The serialized message goes and sits into `a buffer` depending upon the destination address. For each destination, we have a separate buffer.

- Finally, an `I/O thread` that runs in background will pick up some messages from the buffer, combine them to make one single data packet and send it to the broker.

- The broker would save the data in the log file and send back an acknowledgement to I/O thread. 
    - If the I/O thread does not receive an acknowledgement, it will try sending the packet and again wait for an acknowledgement.
    -  If we do not get an acknowledgement at all even after some retries, or get an error message, the I/O thread will give the error back to the send method.
