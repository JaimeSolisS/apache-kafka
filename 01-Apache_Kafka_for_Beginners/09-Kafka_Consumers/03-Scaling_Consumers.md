# Scaling Kafka Consumer

In the earlier lecture, we created a real-time data validation pipeline. Our application reads messages from a Kafka topic, run some validations against them, and write the valid and invalid messages in separate Kafka Topic. Instead of writing to Kafka topics, you can also write them to a database or any other kind of storage system. It is just a matter of changing a few lines of code. 

In all such consume-transform-produce pipelines, your application will create a KafkaConsumer object, subscribe to the appropriate Topic, and start receiving messages, transforming them and writing the results. This process should be seamless.

However, your application will begin falling behind, if the rate at which producers write messages to the Topic, exceeds the rate at which you can transform and write them to external storage.

If you are limited to a single consumer reading and processing the data, your application may fall farther and farther behind and would no longer remain a real-time application. In this scenario, we need to scale consumption from topics.

## How do we scale a consumer process?

We can scale a consumer application by dividing the work among multiple consumers. Just like numerous producers can write to the same Topic, we need to allow various consumers to read from the same Topic.

However, we also need a mechanism to split the data among the consumers, so they work with their own set of data and do not interfere with each other.

How can you do that? This is where the topic partitions are handy. When we have multiple consumers working in a group and reading data from the same Topic, we can easily split the data among consumers by assigning them one or more partitions. In this arrangement, each consumer is attached with a set of Partitions, and they read data only from the assigned partitions.

For example, if you have ten partitions in a topic, and there are two consumers in the same group reading data from the same Topic, Kafka would assign five partitions to each consumer. This arrangement clearly divides the data among the consumers to ensure that they do not read the same message.

In this arrangement, every record is delivered to one and only one consumer in the group, and there is no duplicate processing. This arrangement also adds a restriction on the scalability that would be equal to the number of partitions in a given topic.

For example, if you have ten partitions in a topic, you can add a maximum of ten consumers in a group, each assigned with a single partition to read. However, this limitation can be avoided by careful planning and creating enough number of partitions when we create a new topic.

How can we create a consumer group and add new consumers in the same group? Do we also need to do something for assigning partitions to the consumers? Kafka offers automatic group management and rebalancing of the workload in a consumer group. All we need to do is to set the group id configuration. Kafka automatically forms a consumer group, and it would also add the consumer to the same group if they havethe same group id. Kafka will also take care of assigning partitions to the consumer in the same group.

Membership in a consumer group is maintained dynamically. If a consumer fails, the partitions assigned to it will be reassigned to other consumers in the same group. Similarly, if a new consumer joins the group, partitions will be moved from existing consumers to the new one to maintain the workload balance.

Conceptually, you can think of a consumer group as being a single logical consumer that happens to be made up of multiple processes sharing the workload.

Kafka automatically manages all of this, and the whole process is transparent to users. We, as programmers are responsible for setting the group id configuration and starting a new consumer process either on the same machine or on a separate computer.
