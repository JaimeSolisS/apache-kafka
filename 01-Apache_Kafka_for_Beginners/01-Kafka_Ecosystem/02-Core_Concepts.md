# Apache Kafka Core Concepts

1. Producer
2. Consumer
3. Broker 
4. Cluster
5. Topic
6. Partitions
7. Offset
8. Consumer Groups

These are the core concepts. And the rest of the training is built on your clear understanding of these terminologies and their meaning.

## Producer

`A producer is an application that sends data`. Some people call it data, but we often call them a message or a message record. The message record may have a different meaning and schema or record structure for us. But for Kafka, it is a simple array of bytes.

For example, if I want to send a data file to Kafka, I'll create a producer application, and send eachline of the file as a message. In this case, a message is one line of text, but for Kafka it is just an array of bytes.

Similarly, if I want to send all the records from a database table, I'll post to each row as a message. If you want to send the result of a database query, you are going to create a producer application.The producer is going to fire a query against your database, collect the result and start sending each row as a message.

So while working with Kafka if you want to send some data, you may have to create a producer application. You might find an out of the box ready to use producer which fits your purpose.

## Consumer

`The consumer is an application that receives data`. If the producers are sending data they must be sending it to someone. The consumers are the recipients. But remember, the producers are not going to send data directly to the recipients. They just send it to the Kafka server. And anyone interested in that data should come forward and consume the data from the server.

So an application that is going to request data from the Kafka server is up consumer. And they can ask for the data sent by any producer provided that they have permissions to read it.

So if we come back to the data file example, when I want to read the data file sent by a producer I'll create a consumer application. Then I'll request the Kafka server for the data. The Kafka server will send me some messages. So, the consumer application will receive some lines from the Kafka server. It will process them and again request for some more messages.

This goes in a loop. The consumer keeps asking for the messages, the server keeps giving new records, and this goes in a circle as long as new messages are arriving at the kafka server. 

Now it's up to the consumer application on using the data. They might want to compute some aggregates and send some alerts. That part is up to the consumer application.

## Broker 

`The broker is the Kafka server`. It is just a meaningful name that was given to the Kafka server, and this name makes sense as well. Why? Because all that the server is doing is to `act as a message broker between producer and consumer`. I mean the producer and consumers do not interact directly. They use the Kafka server as `an agent or a broker to exchange messages`.

## Cluster
The Kafka cluster is a group of computers, each running one instance of the Kafka broker.

## Topic

The topic is an arbitrary name given to a data set. You better say - `it's a unique name for a data stream`. If you're coming from a database background, you can also think of it as a database table. 

Once your topic is there, the producers and the consumers are going to send and receive data by the topic.

## Partitions

We already learn that Kafka is a distributed system, and it runs on a cluster of computers. So it is evident that Kafka can easily `break the topic into smaller partitions and store those partitions on different machines`.

This approach will solve the storage capacity problem, and that's where the topic partition means. **A small and independent portion** of the topic.

### How will Kafka decide the number of partitions for a topic?
Kafka doesn't make that decision. `The number of partitions in our topic is a design decision`. So we as an architect are going to decide the number of partitions for each topic. When we create a topic we need to specify the number of partitions that we need, and the Kafka broker will produce it.

> The partition is the smallest unit and it is going to be sitting on a single machine.You cannot break it again, so you must be doing some meaningful estimation to decide the number of partitions for each Topic.

## Offset

It is a unique sequence ID of a message in the partition. `The sequence ID is automatically assigned by the broker to every message record as it arrives in the partition`. Once assigned, These id's are not going to change.**They are immutable**.

So when the first messages are stored in a partition, it gets an offset ID as 0. The next one gets 1 then 2 and so on. This sequencing means that the Kafka **stores messages in the partition, in the order of arrival**. And the offset ID is clearly an arrival order number.

But remember these offsets are local within the partitions. **There is no global ordering in the topic across partitions**. So if you have three partitions in a topic within each partition, the offset is going to start from 0 and increase by 1.

So if you want to locate a specific message, you must read 3 things: 
- Topic name
- Partition number 
- Offset number.

If you have these three things you can directly look at a message in the Kafka cluster.

## Consumer Group

Well it is a `group of consumers`. So multiple consumers can form a group `to share the workload`. You can think of it like there is one big massive work, and you want to divide and assign it in smaller parts among multiple people.

So you'll create a group and the members of the same group are going to share the burden and accomplish the bigger task together.

` the maximum possible parallel consumers are limited by the number of partitions in that topic`. Kafka doesn't allow more than one consumer to read and process data from the same partitions simultaneously. And this restriction is necessary to avoid the double reading of records.