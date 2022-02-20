# Apache Kafka - Storage Architecure

Apache Kafka is a messaging broker. Kafka broker is a middleman between producers and consumers, and it plays three primary responsibilities:

- Receive messages from the producers and acknowledge the successful receipt. 
- Store the message in a log file to safeguard it from potential loss. Storage is also critical to ensure that the consumers can consume it later, and they do not necessarily need to read it in real-time.
- Deliver the messages to the consumers when they request it.

A more elaborated definition could be something like this. *Apache Kafka is a horizontally scalable, fault-tolerant, distributed streaming platform, and it is consciously designed for building real-time streaming data architecture*.

This definition can be broke into three things:

- Kafka Storage Architecture - This discussion will help you to understand some core concepts such as Kafka topics, logs, partitions, replication factor, segments, offsets, and offset-index.
- Kafka cluster architecture - This discussion will help you understand some concepts associated with Cluster formation, Zookeeper, and the Controller.
- Work DIstribution Architecture - In this part, you will learn some concepts such as Leaders, Followers, In Sync Replicas, committed and uncommitted messages.

## Topics & Partitions
Apache Kafka organizes the messages in topics, and the broker creates a log file for each topic to store these messages. However, these log files are partitioned, replicated and segmented. 

### Kafka Topic
A topic is a logical name to group your messages. Like in a database, you must create a table to store your data records, in Kafka, you must create a topic to store messages. So let's create a topic and see a few things in action.

My three node Kafka Cluster is up, and all three brokers are running.
Let me show you what do we have inside the temp directory:
```
├─ tmp/
│  ├─ kafka-logs-0/
│  ├─ kafka-logs-1/
│  ├─ kafka-logs-2/
│  ├─ zookeeper/
```
Kafka needs the zookeeper, and it will be explained later. However, for now, you can think of this directory as a zookeeper data directory where the zookeeper will store its data. 

Other three directories are the data directories of my three Kafka brokers. 
```
├─ tmp/
│  ├─ kafka-logs-0/
│  │  ├─ .lock
│  │  ├─ cleaner-offset-checkpoint
│  │  ├─ log-start-offset-checkpoint
│  │  ├─ meta.properties
│  │  ├─ recovery-point-offset-checkpoint
│  │  ├─ replication-offset-checkpoint
│  ├─ kafka-logs-1/
```
So these are some initial files. When a broker starts, it creates some initial files. Most of these files would be empty at this point. We haven't created any topic yet, so we do not have any file for a topic. This is an initial state of the Kafka broker. We do not want to get into further details of these files. Our focus is to understand how a topic is organized at the broker level. So, we will create one topic and investigate these directories to know how a topic is physically held at the broker.

So let's create a topic. Here is the command to create a Kafka topic. Kafka topics is a shell script on Linux machine and a batch script on a Windows machine.
```
%KAFKA_HOME%\bin\windows\kafka-topics.bat --create 
                                          --bootstrap-server localhost:9092 
                                          --topic invoice 
                                          --partitions 5 
                                          --replication-factor 3 
                                          --config segment.bytes=1000000
```
- The first argument is to tell Kafka that we want to create a topic. 
- The second argument is the zookeeper coordinates. 
- The third argument is the name of the topic.   

These three arguments are mandatory and fundamental arguments for the command to work. When you create a topic in Apache Kafka, you must specify two more configurations:
- Number of Partitions and Replication Factor.

If you do not provide these parameters, Kafka assumes a default value, but every topic must have some values for these two parameters. Let's execute this command, and then we'll talk about the partitions and replication factor. 

### Topic Partitions 

In Kafka, a single topic may store millions of messages, and hence, it is not practical to keep all those messages in a single file. The topic partitions are a mechanism to break the topic further into smaller parts.  

For Apache Kafka, a partition is nothing but a physical directory. Apache Kafka creates a separate directory for each topic partition. We created a topic for invoices and `specified five partition`s, that means, Kafka will create `five directories for the invoice topic`. You can see it here. We got five new directories because we created five partitions for the invoice topic. 

```
─ tmp/
│  ├─ kafka-logs-0/
│  │  ├─ invoice-0/
│  │  ├─ invoice-1/
│  │  ├─ invoice-2/
│  │  ├─ invoice-3/
│  │  ├─ invoice-4/
│  │  ├─ .lock
│  │  ├─ cleaner-offset-checkpoint
│  │  ├─ log-start-offset-checkpoint
│  │  ├─ meta.properties
│  │  ├─ recovery-point-offset-checkpoint
│  │  ├─ replication-offset-checkpoint
```

So, Kafka topic partitions are nothing but directories. You create a Topic with 3 partitions, Kafka broker will create three directories. You asked for 10 partitions, the broker will create 10 directories.

## Topic Replication

### Number of copies for each Partition
**Number of Replicas (15) = Partitions (5) x Replication (3)**

The replication factor specifies how many copies do you want to maintain for each partition. That simply means the replication factor multiplies to the number of partitions. For example, we created one Topic for invoices. While creating the Topic, we specified the `number of partitions as five` and a `replication factor as three`. In this case, Kafka should `create 15 directories`.

We wanted to produce five partitions and maintain three copies of each partition, and that makes 15 directories. We saw 5 directories on broker 0, the other 10 directories were created on the other brokers. So if you look at the other two broker homes, they also have those five directories. Altogether, **we have five partitions on each broker, and in total, we have 15 directories. We term these directories as a partition replica**.

First replica of partition 0:
```
tmp/
├─ kafka-log-0/
│  ├─ invoice-0/
│  ├─ ...
├─ kafka-log-1/
├─ kafka-log-2/
```
Second replica of partition 0:
```
tmp/
├─ kafka-log-0/
├─ kafka-log-1/
│  ├─ invoice-0/
│  ├─ ...
├─ kafka-log-2/
```
Third replica of partition 0:
```
tmp/
├─ kafka-log-0/
├─ kafka-log-1/
├─ kafka-log-2/
│  ├─ invoice-0/
│  ├─ ...
```
Similarly, we have three replicas of partition 1 and so on.

So we understand that the number of partitions and replication factor multiplies and results into directories. And we call them as partition replicas. In our example, we have 15 partition replicas. All these 15 directories are part of the same Topic, but they're distributed among the available brokers.

## Parttion Leaders & Followers 

We can classify topic partition replicas into two categories:
- Leader partitions 
- Follower partitions.

While creating the Topic, we specified the number of partitions as five, and Kafka creates five directories. These five directories are called the `Leader Partitions`. So, the leaders are created first. Then we also specified the replication factor as three. That means Kafka should ensure three copies for each of these five partitions. One is already there, the leader, hence Kafka creates two more directories for each leader, and we call these copies as `Followers`. Remember, the follower is a duplicate copy of the Leader. And all of them are nothing but directories.

If you want to know, which one is the leader and which ones are followers, you can use `Kafka topics command` once again to describe the Topic. The output clearly tells, where does the leader reside for the given partition.

```
%KAFKA_HOME%\bin\windows\kafka-topics.bat --describe  
                                          --bootstrap-server localhost:9092 
                                          --topic invoice

Topic: invoice	TopicId: fhp4CXTzTzihEt9mhspQ_Q	PartitionCount: 5	ReplicationFactor: 3	Configs: segment.bytes=1000000
	Topic: invoice	Partition: 0	Leader: 2	Replicas: 2,1,0	Isr: 1,2,0
	Topic: invoice	Partition: 1	Leader: 1	Replicas: 1,0,2	Isr: 1,2,0
	Topic: invoice	Partition: 2	Leader: 0	Replicas: 0,2,1	Isr: 1,2,0
	Topic: invoice	Partition: 3	Leader: 2	Replicas: 2,0,1	Isr: 1,2,0
	Topic: invoice	Partition: 4	Leader: 1	Replicas: 1,2,0	Isr: 1,2,0
```
At this stage it is essential to understand that there are the leader partitions that are followed by several followers. The number of follower partition depends on the replication factor.

## Kafka Log Segments

So we are done with the directories. These directories are there to hold log files where messages are stored. So the next thing to look at is the log file. The messages are stored within the directories in the log files. However, instead of creating one large file in the partition directory, Kafka creates several smaller files. That means the Kafka log files is split into smaller files known as `segments`.

Let's see the first segment file.

```
.
└── tmp/
    └── kafka-logs-0/
        └── invoice-0/
            ├── 00000000000000000000.index
            ├── 00000000000000000000.log <----
            ├── 00000000000000000000.timeindex
            ├── leader-epoch-checkpoint
            └── partition.metadata
```
Here is the first log segment file. Every topic partition would create the first log segment file, and you can see them in each partition. Now start sending messages and see what happens next.

Let me execute my producer program. We're configuring the producer to send half a million short messages. Average message size is roughly 20 bytes. Let's run it.

AppConfigs:
```
class AppConfigs {
    final static String applicationID = "StorageDemo";
    final static String bootstrapServers = "localhost:9092,localhost:9093";
    final static String topicName = "invoice";
    final static int numEvents = 500000;
```

StorageDemo:
```
public class StorageDemo {

    private static final Logger logger = LogManager.getLogger();

    public static void main(String[] args) {

        logger.info("Creating Kafka Producer...");
        Properties props = new Properties();
        props.put(ProducerConfig.CLIENT_ID_CONFIG, AppConfigs.applicationID);
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfigs.bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<Integer, String> producer = new KafkaProducer<>(props);

        logger.info("Start sending messages...");
        for (int i = 1; i <= AppConfigs.numEvents; i++) {
            producer.send(new ProducerRecord<>(AppConfigs.topicName, i, "Simple Message-" + i));
        }

        logger.info("Finished - Closing Kafka Producer.");
        producer.close();

    }
}
```

Now you can see a bunch of log segment files. 
```
.
└── tmp/
    └── kafka-logs-0/
        └── invoice-0/
            ├── 00000000000000000000.index
            ├── 00000000000000000000.log
            ├── 00000000000000000000.timeindex
            ├── 00000000000000030958.index
            ├── 00000000000000030958.log
            ├── 00000000000000030958.snapshot
            ├── 00000000000000030958.timeindex
            ├── 00000000000000061183.index
            ├── 00000000000000061183.log
            ├── 00000000000000061183.snapshot
            ├── 00000000000000061183.timeindex
            └── ...
```

The next important thing to understand is the logic behind the splitting of segment files. Let's try to understand how this split happens. When the partition receives its first message, it stores the message in the first segment. The next message also goes in the same segment and the segment file continues to grow until the maximum segment limit is reached. As the Kafka broker is writing to the partition, if the segment limit is reached, they close the file and start the new segment. That's how we get multiple segment files in each partition directory. 

The default maximum segment size is either 1 GB of data or a week of data, whichever is smaller. We configured the maximum segment size as one MB when creating the topic, and hence we got multiple segments.

## Message Offsets
- Each Kafka message in the partition is uniquely identified by a 64 bit integer offset.
- Every Kafka message within a single partition is uniquely identified by the offset.

For example, the offset for the first message in the partition would be 0000, the offset for the second message would be 0001, and so on. This numbering also continues across the segments to keep the offset unique within the partition. 

Let us consider that the offset of the last message in the first segment is 30652. Assume that the maximum segment limit is reached, so the Kafka should close this segment and create a new segment file for the next message. The offset for the first message in the new segment continues from the earlier segment and hence, the offset of the first message in the second segment would be 30653. For easy identification, the segment file name is also suffixed by the first offset in that segment.

You should remember that the offset is unique within the partition. If you look across the partitions, the offset is starts from zero in each partition. Hence the offset is a 64 bit integer giving a unique ID to the message in a given partition.

## Message Index
Since the offsets are not unique across the topic,if you want to locate a specific message, you must know at least three things:

- Topic name
- Partition number 
- Offset number

At first sight, this arrangement of uniqueness looks strange. If you're thinking database terms, this unique identification does not make a good sense for locating the messages.

The topic name is like a table name in the database. But partition and offset are just numbers. In database applications, we may want to access data on some critical columns such as invoice_number, customer_name, or customer_id. However you cannot do that in Kafka because messages are not structured into column names. And that's why this arrangement of uniqueness looks strange.

However this numbering may not be a problem for real-time stream processing application. In a stream processing application, the requirement is different. A stream processing application wants to read all messages in a sequence. Let's assume that you have a stream processing application that computes loyalty points for the customer in real-time. 

- The application should read each invoice and calculate loyalty point.
- While computing loyalty points, you need customer_id and the amount, but you must read all the events.

Let's look at the sequence of activities:

1. The application connects to the broker and asks for the messages is starting from the offset zero. Let's assume the broker sense ten messages to the application.

2. The application takes a few milliseconds to compute loyalty points on those 10 invoices. Now it is again ready to process a few more messages. 

3. So, it goes back to the broker and requests for more messages starting from offset 0010. The broker provides another batch of 15 messages.

4. Next time the application requests for another set of messages.  This process continues for the life of the application.

The process explained in this example is the typical pattern of how a stream processing application would work.

In this process, you must have noticed that the consumer application is requesting messages based on the offset. Kafka allows consumers to start fetching messages from a given offset number. This means, if consumer demands for messages beginning at offset hundred, the broker must be able to locate the message for offset hundred. To help brokers rapidly find the message for a given offset, Kafka maintains an `index of offsets`. The index files are also segmented for easy management, and they are also is stored in the partition directory along with the log file segments. You can see them here.
```
.
└── tmp/
    └── kafka-logs-0/
        └── invoice-0/
            ├── 00000000000000000000.index <---
            ├── 00000000000000000000.log
            ├── 00000000000000000000.timeindex
            ├── 00000000000000030958.index <---
            ├── 00000000000000030958.log
            ├── 00000000000000030958.snapshot
            ├── 00000000000000030958.timeindex
            ├── 00000000000000061183.index <---
            ├── 00000000000000061183.log
            ├── 00000000000000061183.snapshot
            ├── 00000000000000061183.timeindex
            └── ...
```

Finally, the last thing in this lecture. The `time index`. Kafka allows consumers to start fetching messages based on the offset number. However, in many use cases, you might want to seek messages based on timestamp. These requirements are as straightforward as you want to read all the events that are created after a specific timestamp. 

To support such needs, Kafka also maintains the timestamp for each message builds a time index to quickly seek the first message that arrived after the given timestamp. The time index is like the offset index, and it is also segmented and stored in the partition directory along with the offset index and log final segment.

You might see a few other types of files in the partition directory. But those files have nothing to do with the data. Kafka creates those files to keep some control information and clean them from time to time. We do not care much about those files.