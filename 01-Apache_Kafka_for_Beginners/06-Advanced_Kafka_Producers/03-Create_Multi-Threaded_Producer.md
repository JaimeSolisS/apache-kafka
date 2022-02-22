# Creating Multi-threaded Producer

## Problem statement

Assume you have multiple data files and you want to send data from all those files to the Kafka Cluster.

So, basically what we want to do is to create one main thread that reads a bunch of data files and create one independent thread to process each data file. For example, if you are supplying three data files to the application, it must create three threads, one for each data file.

Each thread is responsible for reading records from one file and sending them to Kafka cluster in parallel. In all this, we do not want to create a bunch of Kafka producer instances, but as a recommended best practice, we want to share the same Kafka Producer Instance among all threads.

The starter project comes with maven pom file to define the dependencies, preconfigured log4j2.xml, scripts to start the Kafka cluster, create Kafka topic, and start a console consumer to look at the received messages.

Change topic-create.cmd to:
```cmd
%KAFKA_HOME%\bin\windows\kafka-topics.bat --create --bootstrap-server localhost:9092 --topic hello-producer-topic --partitions 5 --replication-factor 3
```

I have also included two sample data files. We will be writing a multithreaded producer to send data from these files to the Kafka cluster.

Same like earlier, the project also includes an `AppConfig class` where I have defined some constants that we will be using in the code that we are going to write: 

```java
class AppConfigs {
    final static String applicationID = "Multi-Threaded-Producer";
    final static String topicName = "nse-eod-topic";
    final static String kafkaConfigFileLocation = "kafka.properties";
    final static String[] eventFiles = {"data/NSE05NOV2018BHAV.csv", "data/NSE06NOV2018BHAV.csv"};
}
```

This time, I have also included a Kafka.properties file to help you understand how can you keep some producer level configurations outside the source code.
```java
bootstrap.servers=localhost:9092,localhost:9093
```

Basically, we want to keep the Kafka Broker coordinates outside the application code. This arrangement allows us to deploy the application and connect to any Kafka cluster by simply changing the connection details in the properties file which resides outside the application.

To solve this problem, we need at least two classes. The first one is required to create a producer thread. We are going to implement Java Runnable Interface to define a class named as `Dispatcher`. The Dispatcher class implements the Runnable interface. The Runnable interface allows us to execute an instance of this class as a separate Thread.

```java
public class Dispatcher implements Runnable {
    private static final Logger logger = LogManager.getLogger();

    @Override
    public void run(){
        
    }
}
```

The second part of the puzzle is to implement a main() method to create threads and start them.I am going to implement a separate class named `DispatcherDemo`, which would have a main() method.

```java
public class DispatcherDemo {
    private static final Logger logger = LogManager.getLogger();
    public static void main(String[] args) {
        
    }
}
```

Now with two classes defined, let's work on the Dispatcher class first. The Dispatcher class will do the following things. Read all records from a given data file. So let's create a private member for the data file. Send all the records from the file to a given Kafka topic. So, let's create a private member for the Kafka topic. In order to send the data to Kafka, we need a Kafka Producer. So let's create a private member for the producer as well.

```java
private String fileLocation; 
private String topicName;
private KafkaProducer<Integer, String> producer; 
```

All these things will be given to the Dispatcher by the main application thread. So, let's create a constructor to take these values:

```java
Dispatcher (KafkaProducer<Integer, String> producer, String topicName, String fileLocation){
    this.producer = producer; 
    this.topicName = topicName; 
    this.fileLocation = fileLocation; 
}
```

Finally, we need to override the run method and implement all the steps that we talked about.The first thing is to get the data file handle. Then create a file scanner. Start scanning each line of the file. We need to send these lines to Kafka. so, let's do producer.send().

You already learned that the send method takes a producer record, We provide the topic name, a message key, which could be null, and finally, the message value. You can adjust your try catch block the way you want. You may also want to add a counter to count the number of lines. Also, put up a log message for starting and one more for finishing.

```java
   @Override
    public void run(){
        logger.info("Start Processing " + fileLocation);
        //Get Data File handle
        File file = new File(fileLocation);
        int counter = 0;

        // Create File Scanner
        try (Scanner scanner = new Scanner (file)) {
            // Scan Each line of the file
            while (scanner.hasNextLine()){
                String line = scanner.nextLine();
                // Send lines to Kafka
                producer.send(new ProducerRecord<>(topicName, null, line));
                counter++;
            }
            logger.info("Finished Sending " + counter + " messages from " + fileLocation);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
```
The runnable class is ready. Now, let's move to the DispatcherDemo and complete that one.

The first thing is to create a Kafka Producer. You already know the steps.

-  We define a properties object. Now, I want to do two things. The first thing is to load some properties that I defined in the Kafka.properties file. You can do that by creating an input stream from a file. Then load it into the properties. Finally,the second part. Put other necessary configurations directly into the properties.

```java
//Define properties object
Properties props = new Properties(); 
try{
    InputStream inputStream = new FileInputStream(AppConfigs.kafkaConfigFileLocation); 
    props.load(inputStream); 
    props.put(ProducerConfig.CLIENT_ID_CONFIG, AppConfigs.applicationID); 
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class); 
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
}catch (IOException e){
    throw new RuntimeException(e); 
}
```


We are now ready to create a Kafka producer. The key would be an integer, we are giving null as the key in the Dispatcher, but that should be fine. The value is a string. Create an instance of the producer supplying the properties.
```java
//Create Kafka Producer
KafkaProducer<Integer, String> producer = new KafkaProducer<Integer, String>(props); 
```

Now we are ready to create dispatcher threads. Each element of the array will hold the handle for the thread so we can join them to wait for their completion. The number of threads must be equal to the number of files. Now, we will create a loop. Inside the loop, create a new thread. We will use the dispatcher class constructor for creating the thread. We will be passing the same producer instance. So all the threads will use the same producer, and that's what we wanted to do. Rest of the parameters are straightforward: Topic name and the file location. In the end, in a separate try-catch block, loop through the thread handles, and join them together. This joining will allow the main thread to wait for all the threads to complete.
Finally, don't forget to close the producer.
```java
//Create Dispatcher Threads
Thread[] dispatchers = new Thread[AppConfigs.eventFiles.length];
logger.info("Starting Dispatcher threads");
for(int i=0; i<AppConfigs.eventFiles.length; i++){
    dispatchers[i] = new Thread(new Dispatcher(producer, AppConfigs.topicName, AppConfigs.eventFiles[i]));
    dispatchers[i].start();
}

try {
    for (Thread t : dispatchers) t.join();
}catch (InterruptedException e) {
    logger.error("Main Thread Interrupted");
} finally {
    producer.close();
    logger.info("Finished Dispatcher Demo"); 
}
```

## Summary

1. We created a runnable dispatcher that takes a Kafka producer instance, topic name, and the file location.

2. Then we created a main method which would create a single instance of Kafka producer.

3. Then create two threads using the runnable Dispatcher and pass the producer instance, along with topic name and file location to the dispatcher thread.

4. Each dispatcher thread will simply send all the lines from the file to the given Kafka topic.

5. The main thread will wait for all the threads to complete

6. Finally close the producer and terminate.

If you want to run this application.

- Start the zookeeper,
- Start all Kafka brokers.
- Create a topic.
- And execute the producer application.
```
[2022-02-22 12:36:08,734] (kafka.examples.DispatcherDemo) INFO - Starting Dispatcher threads 
[2022-02-22 12:36:08,745] (kafka.examples.Dispatcher) INFO - Start Processing data/NSE05NOV2018BHAV.csv 
[2022-02-22 12:36:08,745] (kafka.examples.Dispatcher) INFO - Start Processing data/NSE06NOV2018BHAV.csv 
[2022-02-22 12:36:09,140] (kafka.examples.Dispatcher) INFO - Finished Sending 1881 messages from data/NSE06NOV2018BHAV.csv 
[2022-02-22 12:36:09,149] (kafka.examples.Dispatcher) INFO - Finished Sending 1907 messages from data/NSE05NOV2018BHAV.csv 
[2022-02-22 12:36:09,287] (kafka.examples.DispatcherDemo) INFO - Finished Dispatcher Demo 
```

All the lines from both the files are sent.

## Full Code 

Dispatcher.java
```java
public class Dispatcher implements Runnable {
    private static final Logger logger = LogManager.getLogger();

    private String fileLocation;
    private String topicName;
    private KafkaProducer<Integer, String> producer;

    Dispatcher (KafkaProducer<Integer, String> producer, String topicName, String fileLocation){
        this.producer = producer;
        this.topicName = topicName;
        this.fileLocation = fileLocation;
    }

    @Override
    public void run(){
        logger.info("Start Processing " + fileLocation);
        //Get Data File handle
        File file = new File(fileLocation);
        int counter = 0;

        // Create File Scanner
        try (Scanner scanner = new Scanner (file)) {
            // Scan Each line of the file
            while (scanner.hasNextLine()){
                String line = scanner.nextLine();
                // Send lines to Kafka
                producer.send(new ProducerRecord<>(topicName, null, line));
                counter++;
            }
            logger.info("Finished Sending " + counter + " messages from " + fileLocation);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
```

DispatcherDemo.java
```java
public class DispatcherDemo {
    private static final Logger logger = LogManager.getLogger();
    public static void main(String[] args) {

        //Define properties object
        Properties props = new Properties();
        try{
            InputStream inputStream = new FileInputStream(AppConfigs.kafkaConfigFileLocation);
            props.load(inputStream);
            props.put(ProducerConfig.CLIENT_ID_CONFIG, AppConfigs.applicationID);
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class);
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        }catch (IOException e){
            throw new RuntimeException(e);
        }
        
        //Create Kafka Producer
        KafkaProducer<Integer, String> producer = new KafkaProducer<Integer, String>(props);

        //Create Dispatcher Threads
        Thread[] dispatchers = new Thread[AppConfigs.eventFiles.length];
        logger.info("Starting Dispatcher threads");
        for(int i=0; i<AppConfigs.eventFiles.length; i++){
            dispatchers[i] = new Thread(new Dispatcher(producer, AppConfigs.topicName, AppConfigs.eventFiles[i]));
            dispatchers[i].start();
        }
        try {
            for (Thread t : dispatchers) t.join();
        }catch (InterruptedException e) {
            logger.error("Main Thread Interrupted");
        } finally {
            producer.close();
            logger.info("Finished Dispatcher Demo");
        }
    }
}
```