# Producer Configurations

## Acks & min.insync.replicas

### acks=0 (no acks)
- No response is requested from the broker. 
- If the broker goes offline or an exception happens, we won't know and we'll lose data.
- Useful for data where it's okay to ptentially lose messages.
    - Metrics collection
    - Log collection

### acks=1 (leader acks)
- Leader response is requested, but replication is not a guarantee (happens in the background)
- If an ack is not received, the producer may retry.
- If the leader broker goes offline but replicas haven't replicated the data yet, we have a data loss. 

### acks=all (replicas acks)

- Leader + Replicas ack requested
- Added latency and safety
- No data loss if enough replicas
- Acks=all must be used in conjuction with `min.insync.replicas`.
- `min.insync.replicas` can be set at the broker or topic level (override).  
- `min.insync.replicas=2` implies that at least 2 brokers that are ISR (including leader) must respond that they have the data.
- That means if ypu use `replication.factor=3`, `min.insync=2`  `acks=2`, you can only tolerate 1 broker going down, otherwise the producer will receive an exception on send. 

## Producer Retries

- In case of transient failures, developers are expected to handle
exceptions, otherwise the data will be lost.
- Example of transient failure:
  - NotEnoughReplicasException
- There is a `retries` setting
  - defaults to 0 for Kafka <= 2.0
  - defaults to 2147483647 for Kafka >= 2.1
- The `retry.backoff.ms` setting is by default 100 ms.

### Producer Timeouts

- If retries > 0, for example retries = 2147483647, the producer won't try the request for ever, it's bounded by a timeout.
                       
- For this, you can set an intuitive Producer Timeout (KIP-91 – Kafka 2.1)
- `delivery.timeout.ms = 120 000 ms = 2 minutes`  
- Records will be failed if they can't be acknowledged in delivery.timeout.ms

### Producer Retries Warnings

- In case of retries, there is a chance that messages will be sent out of order
(if a batch has failed to be sent).  
- If you rely on key-based ordering, that can be an issue.  
- For this, you can set the setting while controls how many produce requests
can be made in parallel: `max.in.flight.requests.per.connection` 
  - Default: 5
  - Set it to 1 if you need to ensure ordering (may impact throughput)

In Kafka >= 1.0.0, there's a better solution with idempotent producers!

## Idempotent Producer

Here's the problem: the Producer can introduce duplicate messages in Kafka due to network errors
- In Kafka >= 0.11, you can define a "idempotent producer" which won't introduce duplicates on network error  
- Idempotent producers are great to guarantee a stable and safe pipeline!

### Summary

### Kafka < 0.11 
- `acks=all` (producer level)
    - Ensures data is properly replicated before an ack is received
- `min.insync.replicas=2` (broker/topic level)
    - Ensures two brokers in ISR at least have the data after an ack
- `retries=MAX_INT` (producer level)
    - Ensures transient errors are retried indefinitely
- `max.in.flight.requests.per.connection=1` (producer level)
    - Ensures only one request is tried at any time, preventing message re-ordering in case of retries
### Kafka >= 0.11
- `enable.idempotence=true` (producer level) + `min.insync.replicas=2` (broker/topic level)
   - Implies `acks=all`, `retries=MAX_INT`, `max.in.flight.requests.per.connection=l if Kafka 0.11 or 5 if Kafka >= 1.0`
    - while keeping ordering guarantees and improving performance!
<p> </p>  

-   Running a "safe producer" might impact throughput and latency, always test for your use case

# Safe Producer
Add these configs in Twitter Producer

```java
// create safe Producer
properties.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
properties.setProperty(ProducerConfig.ACKS_CONFIG, "all");
properties.setProperty(ProducerConfig.RETRIES_CONFIG, Integer.toString(Integer.MAX_VALUE));
properties.setProperty(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "5"); // kafka 2.0 >= 1.1 so we can keep this as 5. Use 1 otherwise.
```

## Message Compression

- Producer usually sends data that is text-based, for example with JSON data.  
- In this case, it is important to apply compression to the producer.  
- Compression is enabled at the Producer level and doesn't require any
configuration change in the Brokers or in the Consumers
- `compression.type` can be '`none`' (default), '`gzip`', '`Iz4`','`snappy`'  
- Compression is more effective the bigger the batch of message being sent
to Kafka!
- Benchmarks [in this link](https://blog.cloudflare.com/squeezing-the-firehose/).
- The compressed batch has the following advantage:
  - Much smaller producer request size (compression ratio up to 4x!)
  -  Faster to transfer data over the network => less latency
  -  Better throughput
  - Better disk utilisation in Kafka (stored messages on disk are smaller)
- Disadvantages (very minor):
  - Producers must commit some CPU cycles to compression
  - Consumers must commit some CPU cycles to decompression
- Overall:
  - Consider testing snappy or Iz4 for optimal speed / compression ratio
 ### Recomendation 
- Find a compression algorithm that gives you the best performance for
your specific data. Test all of them!
- Always use compression in production and especially if you have high
throughput
-  Consider tweaking `linger.ms` and `batch.size` to have bigger batches,
and therefore more compression and higher throughput

## Producer Barching

- By default, Kafka tries to send records as soon as possible.
  - It will have up to 5 requests in flight, meaning up to 5 messages individually
  sent at the same time.
  - After this, if more messages have to be sent while others are in flight, Kafka is
  smart and will start batching them while they wait to send them all at once.
- This smart batching allows Kafka to increase throughput while
maintaining very low latency.
- Batches have higher compression ratio so better efficiency.

### Linger.ms
- `Linger.ms`: Number of milliseconds a producer is willing to wait
before sending a batch out (default 0).
- By introducing some lag (for example linger.ms=5), we increase the
chances of messages being sent together in a batch.
- So at the expense of introducing a small delay, we can increase
throughput, compression and efficiency of our producer.
- If a batch is full (see batch.size) before the end of the linger.ms period,
it will be sent to Kafka right away.

### Batch.size
- `batch.size`: Maximum number of bytes that will be included in a
batch. The default is 16KB.
- Increasing a batch size to something like 32KB or 64KB can help
increasing the compression, throughput, and efficiency of requests
- Any message that is bigger than the batch size will not be batched
- A batch is allocated per partition, so make sure that you don't set it
to a number that's too high, otherwise you'll run waste memory!
- (Note:You can monitor the average batch size metric using Kafka
Producer Metrics)

#  High Throughput Producer
- We'll add `snappy` message compression in our producer
- `snappy` is very helpful if your messages are text based, for example log
lines or JSON documents
- `snappy` has a good balance of CPU / compression ratio
- We'll also increase the `batch.size` to 32KB and introduce a small delay
through `linger.ms` (20 ms)

```java
// high throughput producer (at the expense of a bit of latency and CPU usage)
properties.setProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
properties.setProperty(ProducerConfig.LINGER_MS_CONFIG, "20");
properties.setProperty(ProducerConfig.BATCH_SIZE_CONFIG, Integer.toString(32*1024)); // 32 KB
```
