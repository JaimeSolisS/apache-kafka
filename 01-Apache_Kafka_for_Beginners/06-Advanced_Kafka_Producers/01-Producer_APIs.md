# Producer APIs

## Horizontal and Vertical Scalability 
Apache Kafka was designed with the scalability in mind, and scaling a Kafka application is straightforward.

If you consider the POS example, then each POS system can create a KafkaProducer object and send the invoices. Multiple POS systems can send invoices in parallel. At the cluster end, it is the Kafka broker that receives the messages and acknowledges the successful receipt of the message. So, if you have hundreds of producers sending messages in parallel, you may want to increase the number of brokers in your Kafka cluster.

A single Kafka broker can handle hundreds of messages or maybe thousands of messages per second. However, you can increase the number of Kafka brokers in your cluster and support hundreds of thousands of messages to be received and acknowledged. On the producer side, you can keep adding the new producers to send the messages to the Kafka server in parallel. This arrangement provides linear scalability by merely adding more producers and brokers. This approach works perfectly for scaling up your overall streaming bandwidth.

However, you also have an opportunity to scale an individual producer using multithreading technique. A single producer thread is good enough to support the use cases where the data is being produced at a reasonable pace. However, some scenarios may require parallelism at the individual producer level as well. You can handle such requirements using multithreaded Kafka producer.

The multithreading scenario may not apply to applications that do not frequently generate new messages.

For example, an individual POS application would be producing an invoice every 2-3 minutes. In that case, a single thread is more than enough to send the invoices to the Kafka cluster. However, if you have an application that generates or receives data at high speed and wants to send it as quickly as possible, you might want to implement a multithreaded application.

## Producer Multi-Threading Scenario 

Let's assume a stock market data provider application. The application receives tick by tick stock data packets from the stock exchange over a TCP/IP socket. The data packets are arriving at high frequency. So, you decided to create a multithreaded data handler.

The main thread listens to the Socket and reads the data packet as they arrive. The main thread immediately handovers the data packet to a different thread for sending the data to the Kafka broker and starts reading the next data packet. The other threads of the application are responsible for uncompressing the packet, reading individual messages from the data packet, validating the message, and sending it further to the Kafka broker.

Similar scenarios are common in many other applications where the data arrives at high speed, and you may need multiple application threads to handle the load. Kafka producer is `thread-safe`. So, your application can share the same producer object across multiple threads and send messages in parallel using the same producer instance.

It is not recommended to create numerous producer objects within the same application instance. Sharing the same producer across the threads will be faster and less resource intensive.

## At Least Once vs At Most Once

Apache Kafka provides message durability guaranties by committing the message at the partition log. The durability simply means, once the data is persisted by the leader broker in the leader partition, we can't lose that message till the leader is alive. However, if the leader broker goes down, we may lose the data.

To protect the loss of records due to leader failure, Kafka implements replication using followers. The followers will copy messages from the leader and provide fault tolerance in case of leader failure. In other words, when the data is persisted to the leader as well as the followers in the ISR list, we consider the message to be fully committed. Once the message is fully committed, we can't lose the record until the leader, and all the replicas are lost, which is an unlikely case.

However, in all this, we still have a possibility of committing duplicate messages due to the producer retry mechanism. As we learned in the earlier section, if the producer I/O thread fails to get a success acknowledgment from the broker, it will retry to send the same message.

Now, assume that the I/O thread transmits a record to the broker. The broker receives the data and stores it into the partition log. The broker then sends an acknowledgment for the success,
and the response does not reach back to the I/O thread due to a network error. In that case,the producer I/O thread will wait for the acknowledgment and ultimately send the record again assuming a failure. The broker again receives the data, but it doesn't have a mechanism
to identify that the message is a duplicate of an earlier message. Hence,the broker saves the duplicate record causing a duplication problem.

This implementation is known as `at-least-once` semantics, where we cannot lose messages because we are retrying until we get a success acknowledgment. However, we may have duplicates because we do not have a method to identify a duplicate message. For that reason, Kafka is said to provide at-least-once semantics.

Kafka also allows you to implement `at-most-once semantics`. You can achieve at-most-once by configuring the retries to zero. In that case, you may lose some records, but you will never have a duplicate record committed to Kafka logs.

## Exactly Once 

We already learned that Kafka is at-least once system by default. And you can configure it to get at-most once.

However, some use cases want to implement `exactly-once` semantics. I mean, we don't lose anything, and at the same time, we don't create duplicate records. To meet exactly once requirement, Kafka offers an *idempotence* producer configuration. All you need to do is to enable idempotence, and Kafka takes care of implementing exactly-once. To enable idempotence, you should set the `enable.idempotence=true` producer configuration.

Once you configure the idempotence, the behavior of the producer API is changed. There are many things that happen internally, but at a high level, the producer API will do two things.

1. Internal ID for Producer Instance - It will perform an initial handshake with the leader broker and ask for a unique producer id. At the broker side, the broker dynamically assigns a unique ID to each producer.
2. Message Sequence Number - The next thing that happens is the message sequencing. The producer API will start assigning a sequence number to each message. This sequence number starts from zero and monotonically increments per partition.

Now, when the I/O thread sends a message to a leader, the message is uniquely identified by the producer id and a sequence number. The broker knows that the last committed message sequence number is X, and the next expected message sequence number is X+1. This allows the broker to identify duplicates as well as missing sequence numbers. So, setting `enable.idempotence=true` will help you ensure that the messages are neither lost not duplicated. How exactly it happens is not much relevant. We leave that on producer API and broker. All you need to do is to set the configuration to activate this behavior.

However, you must always remember one thing. If you are sending duplicate messages at your application level, this configuration cannot protect you from duplicates. That should be considered as a bug in your application. Even if two different threads or two producer instances are sending duplicates, that too is an application design problem. The idempotence is only guaranteed for the producer retries. And you should not try to resend the messages at the application level. Idempotence is not guaranteed for the application level message resends. Or duplicates send by the application itself.
