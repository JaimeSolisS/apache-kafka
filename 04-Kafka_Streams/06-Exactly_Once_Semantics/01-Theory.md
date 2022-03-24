# What is exactly once? 

- Exactly once is the ability to guarantee that `data processing on each
message will happen only once`, and that `pushing the message back to
Kafka will also happen effectively only once` (Kafka will de-dup).
- Guaranteed when `both input and output system is Kafka`, not for Kafka
to any external systems.
- You can only get Exactly Once Semantics if your Kafka
brokers are of version >= 0.11 and your Kafka Streams
Client is of version >= 0.11

# How does Kafka solve the problem? 

- Without getting in too much engineering details:
    - `The producers are now idempotent` (if the same message is sent twice or more due to retries, Kafka will make sure to only keep one copy of it).
    - `You can write multiple messages to different Kafka topics as part of one transaction` (either all are written, or none is written). This is a new advanced API.

# What's the problem with At Least Once Semantics?
- Cases when it's **not acceptable** to have at least once:
  - Getting the exact count by key for a stream
  - Summing up bank transactions to compute a person's bank balance
  - Any operation that is not idempotent
  - Any financial computation
- Cases when it's **acceptable** at have at least once:
  - Operations on time windows (because the time itself can be vague)
  - Approximate operations (counting the number of times an IP hits a webpage to detect attacks and web scraping).
  - Idempotent operatiopns (such as max, min, etc...)

# How to do exactly once in Kafka Streams?

- One additional line of code
```java
Properties props new Properties (); 
...
props.put (StreamsConfig.PROCESSING GUARANTEE_CONFIG, StreamsConfig.EXACTLY ONCE);
...
KafkaStreams streams = new KafkaStreams (builder, props);
```

## Trade-off 
- Results are published in transactions, which might incur a small latency
- You fine tune that setting using `commit.interval.ms`