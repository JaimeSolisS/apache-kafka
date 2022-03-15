# Kafka Connect 

Four Common Kafka Use Cases:  

- Source => Kafka  **Producer API**                         `Kafka Connect Source`
- Kafka =>   Kafka* **Consumer, Producer API**     `Kafka Streams`
- Kafka =>   Sink **Consumer API**                          `Kafka Connect Sink`
- Kafka =>   App Consumer API

*You have a topic in Kafka and you want to create another topic in Kafka from it.

Kafka Connect simplify and improve getting data in and out of Kafka.

- Programmers always want to import data from the same sources:
    - Databases, JDBC, Couchbase, GoldenGate, SAP HANA, Blockchain, Cassandra,
 DynamoDB, FTP, IOT, MongoDB, MQTT, RethinkDB, Salesforce, Solr, SQS,
 Twitter, etc...
- Programmers always want to store data in the same sinks:
    - S3, ElasticSearch, HDFS, JDBC, SAP HANA, DocumentDB, Cassandra,
  DynamoDB, HBase, MongoDB, Redis, Solr, Splunk, Twitter...

Kafka Connect is basically a set of connectors. This existing connectors allow us for example, to get data from our database straight into KAfka and then that data to ElasticSearch.

So Kafka Connect is don't rewrite code that someone has already written, use someone else connector with your own configuration.

# Architecture Design
![Apache Kafka engine](https://www.researchgate.net/publication/341904461/figure/fig7/AS:898688352260106@1591275408760/Overview-of-the-Apache-Kafka-streaming-engine-16.ppm)

Data comes from your source, goes to Connect cluster made out of workers and pushes data to a cluster. A bunch of streams API (Spark, Kafka Streams) transforms data and pushes into Kafka again and finally Connect cluster pulls the data from Kafka and writes it to the sinks that are configured. 

# Connectors, Configuration, Tasks, Workers

## High Level
- Source Connectors to get data from Common Data Sources
- Sink Connectors to publish that data in Common Data Stores
- Makes it easy to quickly get their data reliably into Kafka
- Part of your ETL pipeline
- Scaling made easy from small pipelines to company-wide pipelines
- Re-usable code!

# Concepts

- Kafka Connect Cluster has multiple loaded `Connectors`
   - Each connector is a re-usable piece of code (java jars)
   - Many connectors exist in the open source world, leverage them!
-  Connectors + **User Configuration**  => `Tasks`
   - A task is linked to a connector configuration
   - A job configuration may spawn multiple tasks
- Tasks are executed by Kafka Connect `Workers` (servers)
   - A worker is a single java process
   - A worker can be standalone or in a cluster

# Standalone vs Distributed Mode
- Standalone:
    - A single process runs your connectors and tasks
    - Configuration is bundled with your process
    - Very easy to get started with, `useful for development and testing`
    -  Not fault tolerant, no scalability, hard to monitor
-  Distributed:
   - Multiple workers run your connectors and tasks
   - Configuration is submitted using a REST API
   - Easy to scale, and fault tolerant (rebalancing in case a worker dies)
   - Useful for `production deployment of connectors`

# List of available connectors
- There are many source and sink connectors
- The best place to find them is the [Confluent website](https://www.confluent.io/hub/).
