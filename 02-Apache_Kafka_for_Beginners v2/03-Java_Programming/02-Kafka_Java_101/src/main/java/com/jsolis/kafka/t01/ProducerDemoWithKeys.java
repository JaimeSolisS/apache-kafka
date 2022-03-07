package com.jsolis.kafka.t01;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ProducerDemoWithKeys {
    public static void main(String[] args) {

        Logger logger = LoggerFactory.getLogger(ProducerDemoWithKeys.class);

        String bootstrapServers = "localhost:9092";
        // create Producer properties
        Properties properties = new Properties ();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName ());
        properties.setProperty(ProducerConfig. VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName ());

        // create the producer
        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(properties);

        for (int i=0; i<10; i++ ) {

            String topic = "first_topic";
            String value = "Hello World " + Integer.toString(i);
            String key = "id_" + Integer.toString(i);
            // create a producer record
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, key,  value);

            logger.info("Key: " + key); // log the key
            // send data - asynchronous
            producer.send (record, new Callback() {
                public void onCompletion (RecordMetadata recordMetadata, Exception e) {
                    // executes every time a record is successfully sent or an exception is thrown
                    if (e == null) {
                        // the record was successfully sent
                        logger.info("Received new metadata. \n" +
                                "Topic:" + recordMetadata.topic() + "\n" +
                                "Partition: " + recordMetadata.partition() + "\n" +
                                "Offset: " + recordMetadata.offset() + "\n" +
                                "Timestamp: " + recordMetadata.timestamp());
                    } else {
                        logger.error("Error while producing", e);
                    }
                }
            });
            //flush data
            producer.flush();
        }
        // flush and close producer
        producer.close();
    }
}
