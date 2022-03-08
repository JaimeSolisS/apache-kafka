# Kafka Twitter Producer

We need to do 3 things:
1. Create a twitter client
2. Create a Kafka Producer
3. Loop to send tweets to kafka

Create the constructor and a run method
```java 
public class TwitterProducer {
    public TwitterProducer(){}
    public static void main(String [] args) {
        new TwitterProducer().run();
    }
    public void run(){
       // create a twitter client

       // create a kafka producer

       // loop to send tweets to kafka
    }
```
## Create Twitter Client

Copy from HBC Quickstart and add your keys and tokens and create the client. 
```java
    public Client createTwitterClient(){
        /** Set up your blocking queues: Be sure to size these properly based on expected TPS of your stream */
        BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>(100000);

        /** Declare the host you want to connect to, the endpoint, and authentication (basic auth or oauth) */
        Hosts hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);
        StatusesFilterEndpoint hosebirdEndpoint = new StatusesFilterEndpoint();
        // Optional: set up some followings and track terms
        //List<Long> followings = Lists.newArrayList(1234L, 566788L);
        List<String> terms = Lists.newArrayList("kafka");
        //hosebirdEndpoint.followings(followings);
        hosebirdEndpoint.trackTerms(terms);

        // These secrets should be read from a config file
        Authentication hosebirdAuth = new OAuth1(Secrets.consumerKey, Secrets.consumerSecret, Secrets.token, Secrets.secret);

        ClientBuilder builder = new ClientBuilder()
            .name("Hosebird-Client-01")          // optional: mainly for the logs
            .hosts(hosebirdHosts)
            .authentication(hosebirdAuth)
            .endpoint(hosebirdEndpoint)
            .processor(new StringDelimitedProcessor(msgQueue));
           // .eventMessageQueue(eventQueue);    // optional: use this if you want to process client events

        Client hosebirdClient = builder.build();
        return hosebirdClient;
    }
```

Create the client in the run function declared above and extract the message queue and pass it as a parameter.
```java
public void run() {
        /** Set up your blocking queues: Be sure to size these properly based on expected TPS of your stream */
        BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>(1000);

        // create a twitter client
        Client client = createTwitterClient(msgQueue);
        // Attempts to establish a connection.
        client.connect();

        // create a kafka producer

        // loop to send tweets to kafka
    }

    public Client createTwitterClient(BlockingQueue<String> msgQueue){
        /** Declare the host you want to connect to, the endpoint, and authentication (basic auth or oauth) */
        Hosts hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);
        StatusesFilterEndpoint hosebirdEndpoint = new StatusesFilterEndpoint();
        // Optional: set up some followings and track terms
        //List<Long> followings = Lists.newArrayList(1234L, 566788L);
        List<String> terms = Lists.newArrayList("kafka");
        //hosebirdEndpoint.followings(followings);
        hosebirdEndpoint.trackTerms(terms);

        // These secrets should be read from a config file
        Authentication hosebirdAuth = new OAuth1(Secrets.consumerKey, Secrets.consumerSecret, Secrets.token, Secrets.secret);

        ClientBuilder builder = new ClientBuilder()
            .name("Hosebird-Client-01")          // optional: mainly for the logs
            .hosts(hosebirdHosts)
            .authentication(hosebirdAuth)
            .endpoint(hosebirdEndpoint)
            .processor(new StringDelimitedProcessor(msgQueue));
           // .eventMessageQueue(eventQueue);    // optional: use this if you want to process client events

        Client hosebirdClient = builder.build();
        return hosebirdClient;
    }
```
## Test Client
To test if the client works, we have to make a loop for polling the data and stoping the client when we get an exception.
```java
    public void run() {
        logger.info("Setup");
        /** Set up your blocking queues: Be sure to size these properly based on expected TPS of your stream */
        BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>(1000);

        // create a twitter client
        Client client = createTwitterClient(msgQueue);
        // Attempts to establish a connection.
        client.connect();

        // create a kafka producer

        // loop to send tweets to kafka
        // on a different thread, or multiple different threads....
        while (!client.isDone()) {
            String msg = null;
            try {
                msg = msgQueue.poll(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
                client.stop();
            }
            if (msg != null){
                logger.info(msg);
            }
        }
        logger.info("End of application");
    }
```

Run the code and we should establish a connection and start seing tweets
```
[main] INFO com.jsolis.kafka.twitter.TwitterProducer - Setup
[main] INFO com.twitter.hbc.httpclient.BasicClient - New connection executed: Hosebird-Client-01, endpoint: /1.1/statuses/filter.json?delimited=length&stall_warnings=true
[hosebird-client-io-thread-0] INFO com.twitter.hbc.httpclient.ClientBase - Hosebird-Client-01 Establishing a connection
[hosebird-client-io-thread-0] INFO com.twitter.hbc.httpclient.ClientBase - Hosebird-Client-01 Processing connection data
[main] INFO com.jsolis.kafka.twitter.TwitterProducer - {"created_at":"Tue Mar 08 20:47:53 +0000 2022","id":1501298682230194184,"id_str":"1501298682230194184","text":"We are not going to the moon, the moon is comming...
```

## Create Kafka Producer

Inside run function create the producer.
```java
KafkaProducer<String, String> producer = createKafkaProducer();
```

Create the function, same as before.
```java
public KafkaProducer<String, String> createKafkaProducer(){
    String bootstrapServers = "localhost:9092";
    // create Producer properties
    Properties properties = new Properties ();
    properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName ());
    properties.setProperty(ProducerConfig. VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName ());

    // create the producer
    KafkaProducer<String, String> producer = new KafkaProducer<String, String>(properties);

    return producer;
}
```

Everytime we're done we will do a producer.send() and intercept errors. Update the code to:

```java
if (msg != null){
    logger.info(msg);
    producer.send(new ProducerRecord<>("twitter_tweets", null, msg), new Callback() {
        @Override
        public void onCompletion(RecordMetadata recordMetadata, Exception e) {
            if (e != null){
                logger.error("Something happened", e);
            }
        }
    });
}
```

Create the cmd files to start zookeeper, kafka brokers, create the topic and start a consumer

```cmd
kafka-topics.bat --bootstrap-server localhost:9092 --create --topic twitter tweets --partitions 6 --replication-factor 1
```
```cmd 
kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic twitter_tweets
```
 Start everything and the consumer should start recieving the messages that are being produced. 

 Add a shutdown hook
 ```java
 //...
// create a kafka producer
    KafkaProducer<String, String> producer = createKafkaProducer();

    //add a shutdown hook
    Runtime.getRuntime().addShutdownHook (new Thread(() -> {
        logger.info("stopping application...");
        logger.info("shutting down client from twitter...");
        client.stop();
        logger.info("closing producer...");
        producer.close();
        logger.info("done!");
    }));
//...
 ```
The reason we do close the producer is so that it sends all the messages that it still has in memory to Kafka before the application is shut down.