# ElasticSearch Sink Connector Distributed Mode 

- Goal:
    - Start an ElasticSearch instance using Docker
    - Sink a topic with multiple partitions to ElasticSearch
    - Run in distributed mode with multiple tasks
- Learning:
    - Learn about the `tasks.max` parameter
    - Understand how Sink Connectors work

## Source connectors
Start our kafka cluster
```sh
docker-compose up kafka-cluster elasticsearch postgres
```
Make sure elasticsearch is working at http://127.0.0.1:9200/
```json
{
  "name" : "Hermes",
  "cluster_name" : "elasticsearch",
  "cluster_uuid" : "hIGxRnEbTP-BeNJCX--bQA",
  "version" : {
    "number" : "2.4.3",
    "build_hash" : "d38a34e7b75af4e17ead16f156feffa432b22be3",
    "build_timestamp" : "2016-12-07T16:28:56Z",
    "build_snapshot" : false,
    "lucene_version" : "5.5.2"
  },
  "tagline" : "You Know, for Search"
}
```
If you see something like this, that means elasticsearch cluster is running.

we need to look at the configuration of the ElasticSearch Connector.
```properties
# Basic configuration for our connector
name=sink-elastic-twitter-distributed
connector.class=io.confluent.connect.elasticsearch.ElasticsearchSinkConnector
# We can have parallelism here so we have two tasks!
tasks.max=2
topics=demo-3-twitter
# the input topic has a schema, so we enable schemas conversion here too
key.converter=org.apache.kafka.connect.json.JsonConverter
key.converter.schemas.enable=true
value.converter=org.apache.kafka.connect.json.JsonConverter
value.converter.schemas.enable=true
# ElasticSearch connector specific configuration
# # http://docs.confluent.io/3.3.0/connect/connect-elasticsearch/docs/configuration_options.html
connection.url=http://elasticsearch:9200
type.name=kafka-connect
# because our keys from the twitter feed are null, we have key.ignore=true
key.ignore=true
# some (dummy) settings we need to add to ensure the configuration is accepted
# The bug is tracked at https://github.com/Landoop/fast-data-dev/issues/42
topic.index.map="demo-3-twitter:index1"
topic.key.ignore=true
topic.schema.ignore=true
```

Copy this and create a new connector on the UI.  Visualize the data at
http://127.0.0.1:9200/_plugin/dejavu. You should see all of twitter data after selecting type kafka-connect. 

You can query them as 
Only Retweets:
```json
{
  "query":{
    "term" : {
      "is_retweet" : true
		}
  }
}
```
High Friends Count:
```json
{
  "query":{
    "range" : {
      "user.friends_count" : {
          "gt" : 500
      }
		}
  }
}
```
