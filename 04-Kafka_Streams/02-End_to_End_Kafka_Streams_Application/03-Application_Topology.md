# WordCount Streams App Topology

1. `Stream` from Kafka                                                                                                      < null, "Kafka Kafka Streams">
2. `MapValues` lowercase                                                                                                  < null,"kafka kafka streams" >
3. `FlatMapValues` split by space                                                     < null, “kafka">, <null,"kafka">, <null,"streams">
4. `SelectKe`y to apply a key                                        < “kafka" ,"kafka">, <"kafka","kafka">, <"streams",“streams">
5. `GroupByKey` before aggregation                       (< “kafka" ,"kafka">, <“kafka", "kafka">), (<"streams","streams">)
6. `Count` occurrences in each group                                                                               < "kafka" , 2 >, <"streams", 1>
7. `To` in order to write the results back to Kafka­­­­                                                            data point is written to Kafka


```java
StreamsBuilder builder = new StreamsBuilder();
    // 1 - stream from kafka
    KStream<String, String> wordCountInput = builder.stream("word-count-input");

    
    KTable<String, Long> wordCounts = wordCountInput
            // 2 - map values to lowercase
            .mapValues(textLine -> textLine.toLowerCase())
            // 3 - flatmap values split by space
            .flatMapValues(loweredCaseTextLine -> Arrays.asList(loweredCaseTextLine.split(" ")))
            //4 - select key to apply a key (we discard the old key)
            .selectKey((key, word) -> word)
            //5 - group by key before aggregation
            .groupByKey()
            //6 - count occurences
            .count(Materialized.as("Counts"));

    // 7 - write the results back to kafka
    wordCounts.toStream().to("word-count-output", Produced.with(Serdes.String(), Serdes.Long()));

    KafkaStreams streams = new KafkaStreams(builder.build(), properties);
    streams.start();
```

## Print the Topology

- Printing the topology at the start of the application is helpful while developing (and even in production) at it helps understand the application flow directly from the first lines of the logs
- Reminder:The topology represents all the streams and processors of your Streams application
```java
KafkaStreams streams new KafkaStreams(builder, props); 
streams.start();
System.out.println(streams.toString()) ;
```

## Closing the application gracefully

- Adding a shutdown hook is key to allow for a graceful shutdown of the Kafka Streams application, which will help the speed of restart.
- This should be in every Kafka Streams application you create
```java
// Add shutdown hook to stop the Kafka Streams threads.
// You can optionally provide a timeout to `close'
Runtime.getRuntime().addShutdownHook (new Thread(streams::close));
```

