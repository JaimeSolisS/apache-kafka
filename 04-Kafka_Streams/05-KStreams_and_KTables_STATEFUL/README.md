# Summary 
- Advanced transformations and operations
    - KTable Stateful Operations 
        - groupBy
    - KGroupedStream / KGroupedTable Operations
        - Count
        - Reduce
        - Aggregate
    - KStreams advanced operations
        - Peek

# KTable 
## GroupBy

- GroupBy allows you to perform more aggregations within a KTable
- It triggers a repartition because the key changes.

```java
// Group the table by a new key and key type
KGroupedTable<String, Integer> groupedTable = table.groupBy(
    (key, value) -> KeyValue.pair (value, value.length ()),
    Serdes.String(), // key (note: type was modified) 
    Serdes.Integer() // value (note: type was modified)  
);         
```

# KGroupedStream / KGroupedTable

## Count
- As a reminder, KGroupedStream are obtained after a `groupBy/groupByKey()` call on a KStream
- `Count` counts the number of record by grouped key.
- If used on KGroupedStream:
  - Null keys or values are ignored
- If used on KGrouped Table:
  - Null keys are ignored
  - Null values are treated as "delete" (tombstones)

## Aggregate

### KGroupedStream
- You need an initializer (of any type), an adder, a Serde and a State Store name (name of your aggregation)
- Example: Count total string length by key
```java
// Aggregating a KGroupedStream (note how the value type changes from String to Long)
KTable<byte[], Long> aggregatedStream = groupedstream.aggregate (
   () -> OL, /* initializer */
    (aggKey, newValue, aggValue) -> aggValue + newValue.length(), /* adder */
    Serdes.Long (), /* serde for aggregate value */
    "aggregated-stream-store" /* state store name
```
### KGroupedTable
- You need an initializer (of any type), an adder, a substractor, a Serde and a State Store name (name of your aggregation)  
- Example: Count total string length by key
```java
// Aggregating a KGroupedStream (note how the value type changes from String to Long)
KTable<byte[], Long> aggregatedStream = groupedStream.aggregate (
    () -> OL, /* initializer */
    (aggkey, newValue, aggValue) -> aggValue + newValue.length (), /* adder */
    (aggkey, oldValue, aggValue) -> aggValue - oldValue.length (), /* subtractor */
    Serdes.Long(), /* serde for aggregate value */
    "aggregated-table-store" /* state store name */);
```              
## Reduce                      
- Similar to `Aggregate` but the result type has to be the same as an input. 
- (Int, Int) => Int (example: a* b)
- (String, String) => String (example concat(a, b))
```java
// Reducing a KGroupedStream
KTable<String, Long> aggregatedStream = groupedStream.reduce(
       (aggValue, newValue) -> aggValue + newValue, /* adder */
       "reduced-stream-store" /* state store name */);

// Reducing a KGroupedTable
KTable<String, Long> aggregatedTable = groupedTable.reduce(
       (aggValue, newValue) -> aggValue + newValue, /* adder */
       (aggValue, oldValue) -> aggValue - oldvalue, /* subtractor */
       "reduced-table-store" /* state store name */);
```

# KStream
## Peek
- `Peek` allows you to apply a side-effect operation to a KStream and get the same KStream as a result.
- A side effect could be:
   - printing the stream to the console
   - Statistics collection
- Warning: It could be executed multiple times as it is side effect (in case of failures)
```java
KStream<byte[], String> stream = ...;
// Java 8+ example, using lambda expressions
KStream<byte[], String> unmodifiedStream = stream.peek (
(key, value) -> System.out.println ("key=" + key + ", value=" + value));
```

# Summary Diagram

![diagram](https://docs.confluent.io/platform/current/_images/streams-stateful_operations.png)