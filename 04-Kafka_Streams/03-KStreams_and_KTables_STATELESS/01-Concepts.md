# KStreams
- All **inserts**
- Similar to a log
- Infinite 
- Unbounded data streams

# KTables
- All **upserts** on non null values
- Deletes on null values
- Similar to a tables
- Paralled with log compacted topics

# KStream vs KTable
- KStream reading from a topic that's not compacted
- KTable reading from a topic that's log-compacted (aggregations)
- KStream if new data is partial information / transactional
- KTable more if you need a structure that's like a "database table",
where every update is self sufficient
(think – total bank balance)

# Statelss vs Statefull Operations
- `Stateless` means that the result of a transformation only depends on
the data-point you process.
   - Example: a “multiply value by 2" operation is stateless because it doesn't need
   memory of the past to be achieved.
    - 1 => 2
    - 300 => 600
- `Stateful` means that the result of a transformation also depends on an
external information – the `state`
   - Example: a count operation is stateful because your app needs to know what
   happened since it started running in order to know the computation result
   - hello => 1
   - hello => 2

# Streams marked for re-partition 

- As soon as an operation can possibly change the key, the stream will be marked for repartition:
  - Мар
  - FlatMap
  - SelectKey
- So only use these APIS if you need to change the key, otherwise use their counterparts:
  - MapValues
  - FlatMapValues
- Repartitioning is done seamlessly behind the scenes but will incur a performance cost (read and write to Kafka)

# Refresher on Log Compaction
- Log Compaction can be a huge improvement in performance when dealing with KTables because eventually records get discarded
- This means less reads to get to the final state (less time to recover)
- Log Compaction has to be enabled by you on the topics that get
created (source or sink topics)

... 

# KStream & KTable Duality

- `Stream as Table`: A stream can be considered a changelog of a table, where each data record in the stream captures a state change of the table.
- `Table as Stream`: A table can be considered a snapshot, at a point in time, of the latest value for each key in a stream (a stream's data records are key-value pairs).

# Transforming a KTable to a KStream

- It is sometimes helpful to transform a KTable to a Kstream in order to keep a changelog of all the changes to the Ktable (see last lecture on Kstream / Ktable duality)

```java
KTable<byte[], String> table = ...;
// Also, a variant of toStream exists that allows you
|// to select a new key for the resulting stream.
KStream<byte[], String> stream = table.toStream();
```

# Transforming a KStream to a KTable

- Two ways:
  - Chain a groupByKey() and an aggregation step (count aggregate, reduce)
```java
KTable<String, Long> table = usersAndColours.groupByKey().count();
```                  
  - Write back to Kafka and read as KTable
```java
// write to Kafka
stream.to ("intermediary-topic");
// read from Kafka as a table
KTable<String, String> table = builder.table ("intermediary-topic");
```