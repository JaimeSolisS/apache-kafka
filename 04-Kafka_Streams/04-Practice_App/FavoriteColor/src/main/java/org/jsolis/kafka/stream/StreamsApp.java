package org.jsolis.kafka.stream;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.Arrays;
import java.util.Properties;

public class StreamsApp {
    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "favoritecolor-application");
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        // we disable the cache to demonstrate all the "steps" involved in the transformation not recommended in prod
        properties.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, "0");

        StreamsBuilder builder = new StreamsBuilder();

        // Step 1 - stream from kafka
        KStream<String, String> favoriteColorInput = builder.stream("favorite-color-input");


        KStream<String, String> userAndColors = favoriteColorInput
                // 1 - Filter out bad data (ensure that its in correct format)
                .filter((key, value) -> value.contains(","))
                // 2 - get the username and assign this value as key
                .selectKey((key,value)-> value.split(",")[0].toLowerCase())
                // 3 - get the color from value
                .mapValues((key, value) -> value.split(",")[1].toLowerCase())
                // 4 - filter only desired colors
                .filter((user, color) -> Arrays.asList("green", "blue", "red").contains(color));

         // Step 2 - write results to Kafka topic
        userAndColors.to("users-and-colors");

        // Step 3 - read that topic as a KTable so that updates are read correctly
        KTable<String, String> userAndColorsTable = builder.table("users-and-colors");

        // Step 4 -  count the occurrences of colors
        KTable<String, Long> favoriteColors = userAndColorsTable
                // group by color within KTable
                .groupBy((user, color) -> new KeyValue<>(color, color))
                        .count(Materialized.<String, Long, KeyValueStore<Bytes, byte[]>>as("CountsByColors")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(Serdes.Long()));

        // Step 5 - Send the results to a Kafka topic
        favoriteColors.toStream().to("favorite-color-output", Produced.with(Serdes.String(), Serdes.Long()));

        KafkaStreams streams = new KafkaStreams(builder.build(), properties);
        streams.start();

        //Print Topology
        System.out.println(streams.toString());

        // shutdown hook to correctly close the streams application
        Runtime.getRuntime().addShutdownHook (new Thread(streams::close));
    }
}
