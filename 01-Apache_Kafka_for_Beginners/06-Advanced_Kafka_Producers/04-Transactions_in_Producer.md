# Transactions in Kafka

The transactional producer goes one step ahead of idempotent producer and provides the transactional guarantee, i.e., an ability to write to several partitions atomically. The atomicity has the same meaning as in databases, that means, either all messages within the same transaction are committed, or none of them are saved.

Let's create a simple example to understand the implementation details of a transaction in Kafka Producer. Let's take the most simple example that we created earlier (the completed hello producer project) and modify it to play a little with transactions.

In this example, we created one topic and sent some messages to the same topic. The first thing that I want to change in this example is to copy the create-topic script and create another topic.

topic-1-create.cdm:
```cmd
%KAFKA_HOME%\bin\windows\kafka-topics.bat --create --bootstrap-server localhost:9092 --topic hello-producer-1 --partitions 5 --replication-factor 3
```

topic-2-create.cdm:
```cmd
%KAFKA_HOME%\bin\windows\kafka-topics.bat --create --bootstrap-server localhost:9092 --topic hello-producer-2 --partitions 5 --replication-factor 3
```

So now, we have two topics. `hello-producer-1` and `hello-producer-2`.

We are going to implement a transaction that would send some messages to both the topics. When we commit the transaction, the messages will be delivered to both the topics. If we abort or rollback the transaction, our messages should not be sent to any of these topics. That's what atomicity means. 

Implementing transactions requires some mandatory topic level configurations. All topics which are included in a transaction should be configured with the replication factor of at least three, and the `min.insync.replicas` for these topics should be set to at least 2.

We already have a replication factor of three in `create-topic`. So the first requirement is already met. The second requirement is to ensure that the min.insync.replicas is set to at least 2 for these topics. Let's add that configuration to our create-topic command.

```cmd
%KAFKA_HOME%\bin\windows\kafka-topics.bat --create --bootstrap-server localhost:9092 --topic hello-producer-2 --partitions 5 --replication-factor 3 --config min.insync.replicas=2
```

Go to AppConfigs class and make some changes there.Reducer the number of events because it will be easy to verify the results with 2-3 records. Create a constant for both topics. Add one more constant named transaction ID, we will use this value to set TRANSACTIONAL_ID_CONFIG for the producer.

```java
class AppConfigs {
    final static String applicationID = "HelloProducer";
    final static String bootstrapServers = "localhost:9092,localhost:9093";
    final static String topicName1 = "hello-producer-1";
    final static String topicName2 = "hello-producer-2";
    final static int numEvents = 2;
    final static String transaction_id = "Hello-Producer-Trans"; 
}
```

Let me fix that configuration first on HelloPRoducer.java. In the properties, set a transaction id. Setting a TRANSACTIONAL_ID for the producer is a mandatory requirement to implement producer transaction, and there are two critical points to remember here:

1.  When you set the transactional id, idempotence is automatically enabled because transactions are dependent on idempotence.

2. Second and most important one - TRANSACTIONAL_ID_CONFIG must be unique for each producer instance.

What does that mean? That means we can't run two instances of a producer with same transactional id. If you do so, then one of those transactions will be aborted because two instances of the same transaction are illegal.

The primary purpose of the transactional id is to rollback the older unfinished transactions for the same transactional id in case of producer application bounces or restarts.

Each instance can set its own unique transaction id, and all of those would be sending data to the same topic implementing similar transaction but all those transactions would be different and will have their own transaction id. 

Setting a transaction id is a mandatory requirement for the producer to implement a transaction and we should always keep the transaction id outside the producer code in a kafka.properties file as I shown in one of the examples.

Implementing transaction in the producer is a three-step process: 

1. The first step is to initialize the transaction by calling initTransactions(). This method performs the necessary check to ensures that any other transaction initiated by previous instances of the same producer is closed. That means, if an application instance dies, the next instance can be guaranteed that any unfinished transactions have been either completed or aborted, leaving the new instance in a clean state before resuming the work. It also retrieves an internal producer_id that will be used in all future messages sent by the producer. The producer_id is used by the broker to implement idempotence.

```java
KafkaProducer<Integer, String> producer = new KafkaProducer<>(props);
producer.initTransactions();
```
The next step is to wrap all your send() API calls within a pair of beginTransaction() and commitTransaction().

```java
 logger.info("Starting First Transaction ...");
        producer.beginTransaction();
        try{
            for (int i = 1; i <= AppConfigs.numEvents; i++) {
                producer.send(new ProducerRecord<>(AppConfigs.topicName1, i, "Simple Message-" + i));
            }
            logger.info("Committing First Transaction");
            producer.commitTransaction();
        } catch (Exception e) {
            logger.error("Exception in First Transaction. Aborting..."); 
            producer.abortTransaction();
            producer.close(); 
            throw new RuntimeException(e); 
        }
        logger.info("Finished - Closing Kafka Producer.");
        producer.close();
```

In case you receive an exception that you can't recover from, abort the transaction and finally close the producer instance. All messages sent between the beginTransaction() and commitTransaction() will be part of a single transaction. Put some log entries, so you know what is happening there.

We created two topics for this example and current code is sending messages to only one topic, so add a new send() line for topic 2. We don't expect any exceptions, and hence the code is not likely to get into the catch block.

Now, we want to add some code for the rollback scenario. This would be the second transaction.
```java
 logger.info("Starting Second Transaction ...");
        producer.beginTransaction();
        try{
            for (int i = 1; i <= AppConfigs.numEvents; i++) {
                producer.send(new ProducerRecord<>(AppConfigs.topicName1, i, "Simple Message-T2-" + i));
                producer.send(new ProducerRecord<>(AppConfigs.topicName2, i, "Simple Message-T2-" + i));
            }
            logger.info("Committing First Transaction");
            producer.abortTransaction();
        } catch (Exception e) {
            logger.error("Exception in Second Transaction. Aborting...");
            producer.abortTransaction();
            producer.close();
            throw new RuntimeException(e);
        }
```

The loop will again execute twice, and we will send a total of 4 messages. Two messages to each topic. But this time, instead of committing the transaction, we will abort it. So, all the 4 messages that I am sending in this second transaction will rollback. If the begin, commit and abort works correctly, I would receive only four messages sent by the first transaction. The messages sent by the second transactions should not appear in any of the topics.

Start all the cluster services and create both topics. Execute the application: 
```
[2022-02-22 17:44:31,357] (kafka.examples.HelloProducer) - INFO Creating Kafka Producer... 
[2022-02-22 17:44:33,130] (kafka.examples.HelloProducer) - INFO Starting First Transaction ... 
[2022-02-22 17:44:33,184] (kafka.examples.HelloProducer) - INFO Committing First Transaction 
[2022-02-22 17:44:33,332] (kafka.examples.HelloProducer) - INFO Starting Second Transaction ... 
[2022-02-22 17:44:33,333] (kafka.examples.HelloProducer) - INFO Aborting Second Transaction ... 
[2022-02-22 17:44:33,405] (kafka.examples.HelloProducer) - INFO Finished - Closing Kafka Producer. 
```

The first transaction committed, and the second one is aborted.

Now its time to investigate the results with the Kafka console consumer script.

```cmd
%KAFKA_HOME%\bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --from-beginning --whitelist "hello-producer-1|hello-producer-2"
```

If we execute the consumer we should see 4 messages tagged as T1. We shouldn't see any message tagged as T2. 

```
Simple Message-T1-2
Simple Message-T1-1
Simple Message-T1-1
Simple Message-T1-2
```


One final note about the transactions. The same producer cannot have multiple open transactions. You must commit or abort the transaction before you can begin a new one. The commitTransaction() will flush any unsent records before committing the transaction. If any of the send calls failed with an irrecoverable error, that means, even if a single message is not successfully delivered to Kafka, the commitTransaction() call will throw the exception, and you are supposed to abort the whole transaction. In a multithreaded producer implementation, you will call the send() API from different threads. However, you must call the beginTransaction() before starting those threads and either commit or abort when all the threads are complete.

