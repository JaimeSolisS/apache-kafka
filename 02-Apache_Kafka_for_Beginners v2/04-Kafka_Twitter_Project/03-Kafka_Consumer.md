# Kafka Consumer ElasticSearch

Create a new module for Kafka Consumer ElasticSearch. Add the following dependencies in pom file:

```xml
<dependencies>
    <!-- https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high-getting-started-maven.html -->
    <dependency>
        <groupId>org.elasticsearch.client</groupId>
        <artifactId>elasticsearch-rest-high-level-client</artifactId>
        <version>6.4.0</version>
    </dependency>

    <!-- https://mvnrepository.com/artifact/org.apache.kafka/kafka-clients -->
    <dependency>
        <groupId>org.apache.kafka</groupId>
        <artifactId>kafka-clients</artifactId>
        <version>2.8.0</version>
    </dependency>

    <!-- https://mvnrepository.com/artifact/org.slf4j/slf4j-simple -->
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-simple</artifactId>
        <version>1.7.25</version>
    </dependency>
</dependencies>
```

Create a `ElasticSearchConsumer` class. Copy and paste some code because there is no kafka interest to connect to elasticsearch so far.
```java
public class ElasticSearchConsumer {

    public static RestHighLevelClient createClient(){
        String hostname = "";
        String username = "";
        String password = "";

        // don't do if you run a local ES
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials (AuthScope. ANY, new UsernamePasswordCredentials(username, password));

        RestClientBuilder builder = RestClient.builder( new HttpHost(hostname, 443, "https"))
             . setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
            @Override
            public HttpAsyncClientBuilder customizeHttpClient (HttpAsyncClientBuilder httpClientBuilder) {
                return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            }
        });

        RestHighLevelClient client = new RestHighLevelClient(builder);
        return client;
    }

    public static void main(String[] args) {

    }
}
```
So basically we are saying connect to this hostname defined, over HTTPS and use some credentials. What we return at the end is just a RestHighLevelClient, which allows us to insert data into elasticsearch.

# Setup ElasticSearch in the Cloud
 Sign in to deploy a free cluster in [bonsai](https://app.bonsai.io/signup#free). Enter the cluster and go to Access and add the credentials to the script. 

 Invoke createClient() and create an IndexRequets

 ```java
public static void main(String[] args) {
    RestHighLevelClient client = createClient();

    IndexRequest indexRequest = new IndexRequest(
            "twitter",
            "tweets"
    );

}
 ```

Now this IndexRequest will fail unless the twitter index exists. So to make sure that it exists, we go to the bonsai interactive console, and from there  do a put on /twitter And this will create the index. Now what we need to do is actually sending some data and adding some source.

```java
public static void main(String[] args) throws IOException {

    Logger logger = LoggerFactory.getLogger(ElasticSearchConsumer.class.getName());
    RestHighLevelClient client = createClient();

    String jsonString = "{\"foo\" : \"bar\" }";

  IndexRequest indexRequest = new IndexRequest("twitter").source(jsonString, XContentType.JSON);

    IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
    String id = indexResponse.getId();
    logger.info(id);
    
    client.close();
}
```
So all this should just do one thing, is to insert one JSON document into our index and we get back the id from it. Try it. If we get `java.lang.ClassNotFoundException` makde sure the Twitter producer and ElasticSearch Consumer are separated because its dependencies conflict between them or add the following dependency into elasticsearch consumer pom file and delete twitter dependency from parent pom file.
```xml
<dependency>
    <groupId>org.apache.httpcomponents</groupId>
    <artifactId>httpclient</artifactId>
    <version>4.5.5</version>
</dependency>
```
We run the code and we should see the id of the document created.

```
[main] INFO com.jsolis.kafka.elasticsearch.ElasticSearchConsumer - FAZBdX8BeAD9qxXA7fZf
```
If we verify it on elasticsearch and we do a get with the id we should see that the document was inserted successfully.
`GET /twitter/_doc/FAZBdX8BeAD9qxXA7fZf`
```json
{
  "_index": "twitter",
  "_type": "_doc",
  "_id": "FAZBdX8BeAD9qxXA7fZf",
  "_version": 1,
  "_seq_no": 0,
  "_primary_term": 1,
  "found": true,
  "_source": {
    "foo": "bar"
  }
}
```

# Write the consumer & Send to ElasticSearch

Create a static function and paste code we already did for consumer. Obviously, change the groupId. We would pass the topic as argument. 

```java
 public static KafkaConsumer<String, String> createConsumer(String topic){
        String bootstrapServers = "localhost:9092";
        String groupId = "kafka-demo-elasticsearch";

        // create consumer configs
        Properties properties = new Properties ();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName ( ));
        properties.setProperty(ConsumerConfig. VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName ());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); //earliest, latest, none

        // create consumer
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(properties);
        
        // subscribe consumer to topic
        consumer.subscribe(Arrays.asList(topic));

        return consumer;
    }
```

Poll data and add the request and response to the loop. It should look as follows:
```java
public static void main(String[] args) throws IOException {

    Logger logger = LoggerFactory.getLogger(ElasticSearchConsumer.class.getName());
    RestHighLevelClient client = createClient();

    KafkaConsumer<String, String> consumer = createConsumer("twitter_tweets");

    // poll for new data
    while(true){
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100)); // new in Kafka 2.0.0
        for (ConsumerRecord<String, String> tweet : records){

            // where we insert data into ElasticSearch
            IndexRequest indexRequest = new IndexRequest("twitter").source(tweet.value(), XContentType.JSON);
            IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);

            String id = indexResponse.getId();
            logger.info(id);

            // introduce delay on purpose
            try {
                Thread.sleep(1000); 
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    //client.close();
    }
```

Start zookeper, the brokers, make sure the topic is created. Run the producer for a few seconds and then run the consumer for a few seconds as well. You should see the ids of the docs that are being inserted into ElasticSearch and if you perform a GET with some random id you should see the json correclty in ElasticSearch. 

```json 
{
  "_index": "twitter",
  "_type": "_doc",
  "_id": "GgZhdX8BeAD9qxXAPvZv",
  "_version": 1,
  "_seq_no": 5,
  "_primary_term": 1,
  "found": true,
  "_source": {
    "created_at": "Thu Mar 10 19:48:51 +0000 2022",
    "id": 1502008602156048400,
    "id_str": "1502008602156048386",
    "text": "RT @CryptoListy: @TokenTax is a crypto tax software platform. And a full-service #cryptocurrency #tax accounting firm. Calculate your #Bitc…",
    "source": "<a href=\"http://twitter.com/download/android\" rel=\"nofollow\">Twitter for Android</a>",
    "truncated": false,
    ....
}
```