# WordCount
## Streams App Properties

- A stream application, when communicating to Kafka, is leveraging the **Consumer** and **Producer** API.
- Therefore all the configurations we learned before are still applicable.
- `bootstrap.servers`: need to connect to kafka (usually port 9092)
- `auto.offset.reset.config`: set to `earliest` to consume the topic from start
- `application.id`: specific to Streams application, will be used for
    - Consumer `group.id` = application.id (most important one to remember)
    - Default `client.id` prefix
    - Prefix to internal changelog topics
- `default.[key|value].serde` (for Serialization and Deserialization of data)

Create StreamsStarterApp class and add the following properties:
```java
public class StreamStarterApp {

    public static void main(String[] args) {

        Properties properties = new Properties();
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "wordcount-application");
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
    }
}
```