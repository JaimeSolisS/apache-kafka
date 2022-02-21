# Creating your first Kafka Producer

Let's start creating some example to understand the mechanics of sending events to Apache Kafka.

## Problem Statement

Create a simple Kafka Producer code that sends one million string messages to a Kafka topic.

We have a pom.xml similar file with all the required dependencies. I also have a log4j2.xml configuration file.

Other than these basic things, we have added an AppConfig class definition with some static constants:
```java
class AppConfigs {
    final static String applicationID = "HelloProducer";
    final static String bootstrapServers = "localhost:9092,localhost:9093";
    final static String topicName = "hello-producer-topic";
    final static int numEvents = 1000000;
}
```

We will be using these constants in the example, and we'll explain them in a minute.

We've also added some scripts to start your Kafka cluster on your local machine. Change topic-create.cmd to:
```cmd
%KAFKA_HOME%\bin\windows\kafka-topics.bat --create --bootstrap-server localhost:9092 --topic hello-producer-topic --partitions 5 --replication-factor 3
```
We are now ready to write our first producer example.

Let's create a class with a main method, name it as HelloProducer.
```java
public class HelloProducer {
    private static final Logger logger = LogManager.getLogger();

    public static void main(String[] args){
        logger.info("Creating Kafka Producer...");

        /*create a Java properties object and put some necessary configurations in it.*/
        Properties props = new Properties();
        props.put(ProducerConfig.CLIENT_ID_CONFIG, AppConfigs.applicationID);
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfigs.bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        /*...*/
}
```

We defined a logger so we can create some log entries. This one is not mandatory but a good practice to have a logger. Next, we created a main method and placed a log entry.The log entry is to know if the program started.

Sending data to Apache Kafka is a multi-step process. The first step is to `create a Java properties object and put some necessary configurations` in it. Kafka producer API is highly configurable,and we customize the behavior by setting different producer configurations.

In this first example, we set up 4 basic configurations. These are the bare minimum configurations for the producer to work:

- The first configuration is the `CLIENT_ID_CONFIG`, which is a simple string that is passed to the Kafka server. The purpose of the client id is to track the source of the message.
- The second one is `BOOTSTRAP_SERVER_CONFIG`. The BOOTSTRAP_SERVER_CONFIG is a comma-separated list of host/port pairs.The producer will use this information for establishing the initial connection to the Kafka cluster. If you are running on a single node Kafka, you can supply an individual host/port information.The bootstrap configuration is used only for the initial connection. Once connected,the Kafka producer will automatically query for the metadata and discover the full list of Kafka brokers in the cluster. That means you do not need to supply a complete list of Kafka brokers as a bootstrap configuration. However, it is recommended to provide two to three broker addresses of a multimode cluster. Doing so will help the producer to check for the second or third broker in case the first brokers in the list is down.

We have defined all these configuration values as constants in the appConfig Class,and that is why I added this class in the starter project template.

Remaining two mandatory configurations are the `key and the value serializers`.

The first one is the `key/value pair`. A Kafka message must have a key/value structure. That means each message that we want to send to the Kafka server should have a key and a value. You can have a null key, but the message is still structured as a key/value pair.

The second concept is about `serializer`. Kafka messages are sent over the network. So,the key and the value must be serialized into bytes before they are streamed over the network. Kafka producer API comes with a bunch of ready to use serializer classes. In this example, we are setting an IntegerSerializer for the key and a StringSerializer for the message value.

Step two is to create an instance of KafkaProducer.
```java
/*...*/

/*Create an instance of Kafka Producer*/
KafkaProducer<Integer, String> producer = new KafkaProducer<Integer, String>(props);

/*...*/
```
The key for my message is going to be an integer, and the value would be a string. We need to pass the properties that we created earlier to the constructor.

The third step is to start sending messages to Kafka. So what we want to do is to create a loop that executes a million times. Inside the loop, you can send the message.

```java
/*...*/

/*Start Sending messages to Kafka*/
logger.info("Start Sending Messages...");
for (int i = 0; i < AppConfigs.numEvents; i++){
    producer.send(new ProducerRecord<>(AppConfigs.topicName, i, "Simple Message-"+i));
}
logger.info("Finished Sending Messages. Closing Producer");

/*...*/
```

The send method takes a ProducerRecord object and sends it to the Kafka cluster. So, we create a new producer record.The ProducerRecord constructor takes three arguments. The first argument is the `topic name`. The second argument is the `message key`. Let's pass the loop counter as a key. The final argument is the `message` itself. We want to send a simple string message. To keep it unique,
we concatenated the loop counter.

Once this loop completes,you would have sent a million string messages to the Kafka cluster.

The last and final step is to close the producer instance. 
```java
/*...*/

/*Close the producer instance*/
producer.close();

/*...*/
```

The producer functionality is involved, and it does a lot of things internally. We will cover producer internals as we progress with this course. However,it is essential to understand that the producer consists of some buffer space and background I/O thread. If you do not close the producer after sending all the required messages, you will leak the resources created by the producer.

## Summary

Sending messages to Kafka using producer API is a four-step process.
- In the first step you will set some configurations that will control the behavior of your producer API.

- The second step is to create a producer object.

- The third step is to send all the messages.

- Finally,if you have no further messages to send,close the producer.

## Execute the example 
> - Start your zookeeper  
> - Then start all three Kafka brokers.
> - Create the topic.
> - Execute HelloProducer
> - Execute Consumer

## Full Code 
```java
public class HelloProducer {
    private static final Logger logger = LogManager.getLogger();

    public static void main(String[] args){
        logger.info("Creating Kafka Producer...");

        /*create a Java properties object and put some necessary configurations in it.*/
        Properties props = new Properties();
        props.put(ProducerConfig.CLIENT_ID_CONFIG, AppConfigs.applicationID);
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfigs.bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        /*Create an instance of Kafka Producer*/
        KafkaProducer<Integer, String> producer = new KafkaProducer<Integer, String>(props);

        /*Start Sending messages to Kafka*/
        logger.info("Start Sending Messages...");
        for (int i = 0; i < AppConfigs.numEvents; i++){
            producer.send(new ProducerRecord<>(AppConfigs.topicName, i, "Simple Message-"+i));
        }
        logger.info("Finished Sending Messages. Closing Producer");

        /*Close the producer instance*/
        producer.close();
    }
}
```
