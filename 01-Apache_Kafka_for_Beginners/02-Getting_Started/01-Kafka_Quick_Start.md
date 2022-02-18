# Kafka Quick Start 

## 1. Installing Single Node Kafka Cluster
Starting a Kafka cluster is a two step process:
1. Start zookeper server
2. Start Kafka broker

### Confluent Community Kafka
Start zookeper server
```
bin/windows/zookeeper-server-start.bat etc/kafka/zookeeper.properties
```
Start Kafka broker 
```
bin/windows/kafka-server-start.bat etc/kafka/server.properties
```
### Open Source Apache Kafka
Start zookeper server
```
bin/windows/zookeeper-server-start.bat config/zookeper.properties
```
Start Kafka broker 
```
bin/windows/kafka-server-start.bat config/server.properties
```
## 2. Using Producer and Consumer
1. Sending Data File to Kafka  
    I. Create Topic using kafka-topics  
    II. Sending data file using kafka-console-producer
2. Reading Kafka Topic  
    I. Using kafka-console-consumer

### Create a Topic
```
bin/windows/kafka-topics.bat --create --topic test --partitions 1 --replication-factor 1 --bootstrap-server localhost:9092
```
How many partitions?
1. Storage Requeriment
2. Parallel Processing Requirement

**Replication factor**: Number of copies of each partition.

### Kafka Console Producer
```
bin/windows/kafka-console-producer.bat --topic test --broker-list localhost:9092 <../data/sample1.csv
```
Cluster coordinates:
topic tool calls it *bootstrap-server* and the producer calls it *broker-list* but the value is the same

### Kafka Console Consumer

```
bin/windows/kafka-console-consumer.bat --topic test --bootstrap-server localhost:9092 --from-beginning
```

## 3. Instaling Multi-Node Kafka Cluster
### Set up Kafka Cluster
Go to the Kafka home directory and look for the Kafka server properties file. If you are using Confluent Kafka,then you can find it in etc/kafka directory. In the case of open-source Kafka, you should see it in the config directory.  

When you start the Kafka broker, you are going to supply this file as an argument. And the Kafka-server-start command is going to read broker configurations from this file. We are planning to start three brokers, so let us make three copies of this file and give a different name to all of them.

And when we start the three Kafka brokers, we are going to give the first file to the first broker, second file to the second broker, and so on. But before that, we need to modify these files for each broker.  

For example, the first configuration is the broker id. Each broker should have a unique id, and you can define the idea in this config file. But we leave this configuration unchanged for the first broker. So the first broker will get an ID zero. However, since the ID should be unique, we need to modify the second and the third files and give them a different ID.  

server-0.properties
```
############################# Server Basics #############################

# The id of the broker. This must be set to a unique integer for each broker.
broker.id=0
```

server-1.properties
```
############################# Server Basics #############################

# The id of the broker. This must be set to a unique integer for each broker.
broker.id=1
```
server-2.properties
```
############################# Server Basics #############################

# The id of the broker. This must be set to a unique integer for each broker.
broker.id=2
```
The next most critical property is the listener port the port config is commented, and hence the broker gets a default port. This is the port number at which the producers are going to send data to the Kafka broker.  

 Similarly, consumers are also going to request the data using this port. The point is a straight. Kafka broker listens to this port for produce and consumes requests coming from the producer and the consumer. We are going to run three brokers on this same machine. So, all three brokers should be listening to different ports. Hence we uncomment this configuration and change it.  
 
 For the first broker, we will leave the default value 9092. But for the second broker, we will replace it for 9093 similarly for the third broker we are changing it to 9094.

 server-0.properties 
 ```
############################# Socket Server Settings #############################

# The address the socket server listens on. It will get the value returned from 
# java.net.InetAddress.getCanonicalHostName() if not configured.
#   FORMAT:
#     listeners = listener_name://host_name:port
#   EXAMPLE:
#     listeners = PLAINTEXT://your.host.name:9092
listeners=PLAINTEXT://:9092
 ```
  server-1.properties 
 ```
############################# Socket Server Settings #############################

# The address the socket server listens on. It will get the value returned from 
# java.net.InetAddress.getCanonicalHostName() if not configured.
#   FORMAT:
#     listeners = listener_name://host_name:port
#   EXAMPLE:
#     listeners = PLAINTEXT://your.host.name:9092
listeners=PLAINTEXT://:9093
 ```
  server-2.properties 
 ```
############################# Socket Server Settings #############################

# The address the socket server listens on. It will get the value returned from 
# java.net.InetAddress.getCanonicalHostName() if not configured.
#   FORMAT:
#     listeners = listener_name://host_name:port
#   EXAMPLE:
#     listeners = PLAINTEXT://your.host.name:9092
listeners=PLAINTEXT://:9094
 ```

 Now all three brokers will be listening to three different ports, and when we start them, they won't get a port already in use error, right? But in a real life scenario, you will be launching one broker on one machine and other broker on a different computer. In that case, **we do not need to change the port numbers. But running them on a single device would need to assign different ports**. 

The third configuration is the Kafka log directory location. This is the directory location where Kafka is going to store the partition data. When you are running multiple brokers on the same machine, **you should also assign a different directory to each broker**.

server-0 properties
```
############################# Log Basics #############################

# A comma separated list of directories under which to store log files
log.dirs=/tmp/kafka-logs-0
```

server-1 properties
```
############################# Log Basics #############################

# A comma separated list of directories under which to store log files
log.dirs=/tmp/kafka-logs-1
```

server-2 properties
```
############################# Log Basics #############################

# A comma separated list of directories under which to store log files
log.dirs=/tmp/kafka-logs-2
```

We have many more configurations defined in this file. However there are not conflicting with each other, and we leave the default values. You'll learn more about these configurations in the later sections.

Now we are all set to start a Kafka cluster of three brokers on a single machine. Let's do it. The steps are the same as you have done it for a single broker.

Before we start the zookeeper, we want to clean up the data directories. Why? Because we created a single node Kafka broker, sent some data, did few things for the earlier lecture. Now we want to start fresh. And the most straightforward method to do that is to delete the Kafka log directory and the zookeeper data directory.

Start zookeper server
```
bin/windows/zookeeper-server-start.bat etc/kafka/zookeeper.properties
```
Start the brokers
```
bin/windows/kafka-server-start.bat etc/kafka/server-0.properties
```
```
bin/windows/kafka-server-start.bat etc/kafka/server-1.properties
```
```
bin/windows/kafka-server-start.bat etc/kafka/server-2.properties
```
You can run as many brokers as you want on a single machine. All you need to do is to create a separate config file for each broker and start it.

## 4. Using Consumer Group

Create new topic 
```
bin/windows/kafka-topics.bat --create --topic stock-ticks --partitions 3 --replication-factor 1 --bootstrap-server localhost:9092
```

Start console consumers

Start the console consumer who is going to connect to the Kafka cluster using one of the brokers.You do not need to tell all the broker coordinates. Just one is more than enough. Once connected to the cluster, the console consumer is going to be reading data from this topic from the beginning. I'm also going to make this consumer join a group. Let's name it group1. You can use any string name for your group.

```
bin/windows/kafka-console-consumer.bat --topic stock-ticks --bootstrap-server localhost:9092 --from-beginning --group group1
```
Now I'm going to start a new command window and run one more consumer. The command remains the same.

```
bin/windows/kafka-console-consumer.bat --topic stock-ticks --bootstrap-server localhost:9092 --from-beginning --group group1
```

We are all set.

- We have a three node Kafka cluster.
- We have a topic with three partitions.
- We have two consumers running in a group and waiting to read data from these partitions. 

Now I'm going to start a producer and send one data file. The data goes to the Kafka topic. Since the topic is partitioned all the data will be distributed among the three partitions. Some records will come to the first broker in the first partition. Some of them will go to the other two brokers, and hence they will land in the other two partitions. However, we have two consumers in the group, right? Three partitions but two consumers.

So, one of these consumers is going to read data from two partitions. And the other one is going to read the data from the other remaining partition. However, all the data is processed. Make sure you are pushing the data to the same topic which these two consumers are waiting to read.

```
bin/windows/kafka-console-producer.bat --topic stock-ticks --broker-list localhost:9092 <../data/sample1.csv
```
Who processes how many records?
```
Processed a total of 1245 messages
```
```
Processed a total of 662 messages
```
Let's see what is there inside this log file. And we have a tool to do that. Kafka dump log and the files to dump.
```
$ bin/windows/kafka-dump-log.bat --files c:/tmp/kafka-logs-0/stock-ticks-1/00000000000000000000.log
Dumping c:\tmp\kafka-logs-0\stock-ticks-1\00000000000000000000.log
Starting offset: 0
baseOffset: 0 lastOffset: 162 count: 163 baseSequence: -1 lastSequence: -1 producerId: -1 producerEpoch: -1 partitionLeaderEpoch: 0 isTransactional: false isControl: false position: 0 CreateTime: 1645040940403 size: 16339 magic: 2 compresscodec: none crc: 831234578 isvalid: true
baseOffset: 163 lastOffset: 325 count: 163 baseSequence: -1 lastSequence: -1 producerId: -1 producerEpoch: -1 partitionLeaderEpoch: 0 isTransactional: false isControl: false position: 16339 CreateTime: 1645040940411 size: 16325 magic: 2 compresscodec: none crc: 3536677129 isvalid: true
baseOffset: 326 lastOffset: 489 count: 164 baseSequence: -1 lastSequence: -1 producerId: -1 producerEpoch: -1 partitionLeaderEpoch: 0 isTransactional: false isControl: false position: 32664 CreateTime: 1645040940419 size: 16303 magic: 2 compresscodec: none crc: 4022520382 isvalid: true
baseOffset: 490 lastOffset: 657 count: 168 baseSequence: -1 lastSequence: -1 producerId: -1 producerEpoch: -1 partitionLeaderEpoch: 0 isTransactional: false isControl: false position: 48967 CreateTime: 1645040940428 size: 16360 magic: 2 compresscodec: none crc: 1791391201 isvalid: true
baseOffset: 658 lastOffset: 820 count: 163 baseSequence: -1 lastSequence: -1 producerId: -1 producerEpoch: -1 partitionLeaderEpoch: 0 isTransactional: false isControl: false position: 65327 CreateTime: 1645040940433 size: 16381 magic: 2 compresscodec: none crc: 921048667 isvalid: true
```

What do you see? Base offset, last offset, and the total records. And then the next packet. Then the next packet and so on. So I have a total of 821 records in this partition. The record counter starts from offset 0 and goes up to 820.

Let's dump the other partition logs.

```
$ bin/windows/kafka-dump-log.bat --files c:/tmp/kafka-logs-1/stock-ticks-0/00000000000000000000.log
Dumping c:\tmp\kafka-logs-1\stock-ticks-0\00000000000000000000.log
Starting offset: 0
baseOffset: 0 lastOffset: 166 count: 167 baseSequence: -1 lastSequence: -1 producerId: -1 producerEpoch: -1 partitionLeaderEpoch: 0 isTransactional: false isControl: false position: 0 CreateTime: 1645040940406 size: 16368 magic: 2 compresscodec: none crc: 1007069913 isvalid: true
baseOffset: 167 lastOffset: 331 count: 165 baseSequence: -1 lastSequence: -1 producerId: -1 producerEpoch: -1 partitionLeaderEpoch: 0 isTransactional: false isControl: false position: 16368 CreateTime: 1645040940421 size: 16294 magic: 2 compresscodec: none crc: 4054933068 isvalid: true
baseOffset: 332 lastOffset: 423 count: 92 baseSequence: -1 lastSequence: -1 producerId: -1 producerEpoch: -1 partitionLeaderEpoch: 0 isTransactional: false isControl: false position: 32662 CreateTime: 1645040940436 size: 9226 magic: 2 compresscodec: none crc: 356446327 isvalid: true

$ bin/windows/kafka-dump-log.bat --files c:/tmp/kafka-logs-2/stock-ticks-2/00000000000000000000.log
Dumping c:\tmp\kafka-logs-2\stock-ticks-2\00000000000000000000.log
Starting offset: 0
baseOffset: 0 lastOffset: 162 count: 163 baseSequence: -1 lastSequence: -1 producerId: -1 producerEpoch: -1 partitionLeaderEpoch: 0 isTransactional: false isControl: false position: 0 CreateTime: 1645040940399 size: 16305 magic: 2 compresscodec: none crc: 3597686381 isvalid: true
baseOffset: 163 lastOffset: 327 count: 165 baseSequence: -1 lastSequence: -1 producerId: -1 producerEpoch: -1 partitionLeaderEpoch: 0 isTransactional: false isControl: false position: 16305 CreateTime: 1645040940415 size: 16289 magic: 2 compresscodec: none crc: 2188338664 isvalid: true
baseOffset: 328 lastOffset: 491 count: 164 baseSequence: -1 lastSequence: -1 producerId: -1 producerEpoch: -1 partitionLeaderEpoch: 0 isTransactional: false isControl: false position: 32594 CreateTime: 1645040940425 size: 16312 magic: 2 compresscodec: none crc: 3030674233 isvalid: true
baseOffset: 492 lastOffset: 661 count: 170 baseSequence: -1 lastSequence: -1 producerId: -1 producerEpoch: -1 partitionLeaderEpoch: 0 isTransactional: false isControl: false position: 48906 CreateTime: 1645040940431 size: 16366 magic: 2 compresscodec: none crc: 3354792190 isvalid: true
```

Partition 0 has got 424 records. And the partition 2 has got 662 records.  

A log dump is a rich tool. You can even print the whole data set. Now you know which partition has got what. 

**Console Producer**: 1907 records

**Kafka Cluster**  
Broker 0 Partition 1: 821 records   
Borker 1 Parition 0: 424 records  
Broker 2 Partition 2: 662 records   

**Consumer Group1**  
Consumer 1: 662 records (Partition 2)  
Consumer 2: 1245 records (Partition 0 & 1)

# Summary
- We learned that the Kafka cluster will store data in the topic partitions. 
- Each partition is managed by a separate broker as a storage directory, and the actual data sets insideb the log files. 
- You can use the log dump tool to investigate these log files. 
- The next thing that we learn is this **Consumers can work in consumer group to share the work load and try to achieve workload balance** to the extent it is possible.