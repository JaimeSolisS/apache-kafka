# Twitter Source Connector Distributed Mode
- Goal:
    - Gather data from Twitter in Kafka Connect Distributed mode
-   Learning: 
    - Gather real data using https://github.com/Eneco/kafka-connect-twitter 


create the topic we're going to write to
```
docker run --rm -it --net=host landoop/fast-data-dev:2.3.0 bash
kafka-topics --create --topic demo-3-twitter --partitions 3 --replication-factor 1 --zookeeper 127.0.0.1:2181
```
Start a console consumer on that topic
```
kafka-console-consumer --topic demo-3-twitter --bootstrap-server 127.0.0.1:9092
```
Create a new connector on KAFKA CONNECT UI. Copy the properties on twitter-source.properties file with ypur own keys and tokens. 

```properties
# Basic configuration for our connector
name=source-twitter-distributed
connector.class=com.eneco.trading.kafka.connect.twitter.TwitterSourceConnector
tasks.max=1
topic=demo-3-twitter
key.converter=org.apache.kafka.connect.json.JsonConverter
key.converter.schemas.enable=true
value.converter=org.apache.kafka.connect.json.JsonConverter
value.converter.schemas.enable=true
# Twitter connector specific configuration
twitter.consumerkey=redacted
twitter.consumersecret=redacted
twitter.token=redacted
twitter.secret=redacted
track.terms=programming,java,kafka,scala
language=en
```
# Twitter Source Connector not working in Landoop
The create button for this connector in the UI is disabled. It seems this connector is outdated which make the UI crash. You need to ensure there is also a topics (as well as topic property) defined on the connector. If you don't add the topics property the connector will fail to be created. If you don't define a topic property it will default to the topic "tweets". Given topics is a property on the sink connector it seems a bug that this property is mandatory on the source. In addition I also could not get this to work via the landoop ui (the create button was greyed out no matter what I tried) - so had to use REST API client directly to create the connector as shown below. This should work for you 

 POST http://localhost:8083/connectors 

```json
{
"name": "source-twitter-distributed",
"config": {
   "connector.class": "com.eneco.trading.kafka.connect.twitter.TwitterSourceConnector",
    "tasks.max": "1",
    "topics" : "demo-3-twitter",
    "topic": "demo-3-twitter",
    "key.converter": "org.apache.kafka.connect.json.JsonConverter",
    "key.converter.schemas.enable": "true",
    "value.converter": "org.apache.kafka.connect.json.JsonConverter",
    "value.converter.schemas.enable": "true",
    "twitter.consumerkey": "redacted",
    "twitter.consumersecret": "redacted",
    "twitter.token": "redacted",
    "twitter.secret": "redacted",
    "track.terms": "programming,java,kafka,scala",
    "language": "en"
    }
}
```
The consumer should start receiving tweets now. So when we go to the Kafka topics UI, we refresh it. We can now see that our Demo three Twitter topic is getting a lot of data in.