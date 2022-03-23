# Transformations & Operations

# MapValues / Map
- Takes one record and produces one record.

- `MapValues`
    - only affecting values
    - does not change keys
    - does not trigger a repartition
    - For KStreams and KTables

- `Map`
    - Affects both keys and values
    - Triggers a re-partitions
    - For KStreams only

```java
// Java 8+ example, using lambda expressions 
Kstream<byte[], String> uppercased = stream.mapValues (value -> value.toUpperCase()); 
```
# Filter / FilterNot
- Takes one record and produces zero or one record
- `Filter`
    - does not change keys/ values
    - does not trigger a repartition
    - for KStreams and KTables
- `FilterNot`
    - Inverse of FIlter

```java
// A filter that selects (keeps) only positive numbers
KStream<String, Long> onlyPositives = stream.filter((key, value) -> value > 0); 
```

# FlatMapValues / FlatMap
- Takes one record and produces zero, one or more records
- `FlatMapValues`
    - does not change keys • does
    - does not trigger a repartition
    - For KStreams only
  
- `FlatMap`
    - Changes keys
    - triggers a repartitions
    - For KStreams only

```java
// Split a sentence into words.
words = sentences.flatMapValues(value -> Arrays.asList(value.split("\\s+")));
```

# Branch

- Branch (split) a KStream based on one or more predicates
- Predicates are evaluated in order, if no matches, records are dropped
- You get multiple KStreams as a result

```java
KStream<String, Long>[] branches = stream.branch(
    (key, value) -> value > 100, /* first predicate */
    (key, value) -> value > 10, /* second predicate */
    (key, value) -> value > 0  /* third predicate */
);
```

# SelectKey 
- Assigns a new Key to the record (from old key and value)
- marks the data for re-partitioning
- Best practice to isolate that transformation to know exactly where
the partitioning happens.
```java
// Use the first letter of the key as the new key
rekeyed = stream.selectKey((key, value) -> key.substring (0, 1))
```

# Reading from Kafka
- We can read a topic as a KStream, a KTable or a GlobalKTable

```java
KStream<String, Long> wordCounts = builder.stream (
        Serdes.String (), /* key serde */
        Serdes. Long (), /* value serde */
       "word-counts-input-topic" /* input topic */);
```

```java
KTable<String, Long> wordCounts = builder.table (
        Serdes.String (), /* key serde */
        Serdes.Long (), /* value serde */
       "word-counts-input-topic" /* input topic */);
```

```java
GlobalKTable<String, Long> wordCounts = builder.globalTable (
        Serdes.String (), /* key serde */
        Serdes.Long (), /* value serde */
       "word-counts-input-topic" /* input topic */);
```

# Writing to Kafka
- We can write any KStream or KTable back to Kafka
- If we write a KTable back to Kafka, think about creating a log compacted topic

- To:Terminal operation - write the records to a topic
```java
stream.to("my-stream-otuput-topic");
table.to("my-table-output-topic");
```
- Through: write a topic and get a stream / table from the topic
```java
KStream<String, Long> newStream = stream.through("user-clicks-topic"); 
KTable<String, Long> newTable = table.through("my-table-output-topic"); 
```

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