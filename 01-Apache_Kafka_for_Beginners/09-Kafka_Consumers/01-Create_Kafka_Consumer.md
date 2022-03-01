# Kafka Consumer API 

## How to process Kafka stream?

There are many ways to create a rich stream processing applications. Kafka offers you 3 tools for that purpose. 
- Consumer APIs,
- Kafka streams library
- KSQL. 

We can use Kafka consumer APIs to consume data from your Kafka brokers and create your stream processing applications. And that's what we are going to learn in this section. Kafka streams and KSQL are beyond the scope of this beginner's course. These things would require separate training in itself.

## Creating Kafka Consumer Application

In the earlier lecture, we created a POS simulator application that generates a series of invoices and sends them to a Kafka Topic. Now, in this example, we want to implement a miniature form of a real-time data validation service for invoices. So, what we want to do is to read all invoices in real-time. Apply some business rules to validate the invoice. If the validation passed, send them to a Kafka topic of valid invoices. If the validation failed, send them to a Kafka topic of invalid invoices. As a result, we will be able to segregate valid and invalid invoices into two different topics.

We will end the example there. However, the overall system might look something like this:

- All valid records are consumed by a data reconciliation application where the invoices are worked upon to rectify the issues and sent back to the pool of valid records.

- All the correct records are consumed by other applications and microservices to achieve some business objectives.

Let's build a real-time data validation service.

The first thing that we need is a business rule to define an invalid invoice.

### What is an Invalid Invoice?

For our purpose, lets define a simple rule. An invoice is considered invalid if it is marked for home delivery, but a contact number for the delivery address is missing.

## Code

Same like earlier, we got a starter project that comes with necessary dependencies, scripts, a bunch of predefined classes to model invoices, a JSON serializer as well as a deserializer and an AppConfig class.

Create PosValidator class. Creating a Kafka consumer is a four-step process, and it is very similar to creating a Kafka producer.

1) The first step is to create a Java Properties and set required Kafka consumer configurations.

```java
Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.CLIENT_ID_CONFIG, AppConfigs.applicationID);
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfigs.bootstrapServers);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(JsonDeserializer.VALUE_CLASS_NAME_CONFIG, PosInvoice.class);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, AppConfigs.groupID);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
```

Same as producer API, Kafka consumer API is also highly configurable, and we customize the behavior by setting the config parameters.

- The first setting is the client id.
- The second one is the bootstrap server.
- Other two configurations are the key and the value deserializer. The Kafka producer transmits a message after serializing them to raw bytes.

Now at the consumer, we must deserialize the record to an appropriate object. Since the example is intending to read the messages sent by the POS simulator, and the POS simulator used JSON serializer. Hence, the corresponding consumer must be using a JSON deserializer.

- The next configuration, VALUE_CLASS_NAME_CONFIG, is the target deserialized Java Class name. We want our message to be deserialized to a POSInvoice.

The last configs are specific to the following concepts: Kafka Consumer Groups and Kafka offsets and consumer positions

2) Create an instance of KafkaConsumer class.

```java
KafkaConsumer<String, PosInvoice> consumer = new KafkaConsumer<>(consumerProps);
```
My key is coming as String, and the value is coming as PosInvoice. We need to supply the configurations that we created in step one.

3) Subscribe to the topics that we want to read.

```java
 consumer.subscribe(Arrays.asList(AppConfigs.sourceTopicNames));
```

A Kafka consumer can subscribe to a list of topics. However, in this example, we want to read a single topic.

4) Read the messages in a loop. Mostly, this loop would be an infinite loop. Because we are expected to keep reading the records and process them in real-time for the life of the application. This thing should never stop. So, we need an infinite loop. How do we read? 

After subscribing to the topics,the consumer can start requesting the message records by making a call to the poll() method. The poll() method will immediately return an Iterable ConsumerRecords. If there are no records at the broker, it will wait for the timeout. When the timeout expires, an empty ConsumerRecords will be returned. Now, we will loop through all the records that we received. 

```java
while (true) {
            ConsumerRecords<String, PosInvoice> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, PosInvoice> record : records) {
                if (record.value().getDeliveryType().equals("HOME-DELIVERY") &&
                    record.value().getDeliveryAddress().getContactNumber().equals("")) {
                    //Invalid
                    
                } else {
                    //Valid
                    
                }
            }
        }
```

So we have this nice loopw here we can process each record and do whatever we want to do. We wanted to perform some data validation. If the record value delivery type is equal to the home delivery and the record value delivery address contact number is empty, then it is an invalid record. Else, it is a valid record.

But we wanted to send these invalid and valid records to separate Kafka topics. And that would require creating a producer.

So, Create a properties object. Set some mandatory configurations. Then, create a producer.

```java
Properties producerProps = new Properties();
producerProps.put(ProducerConfig.CLIENT_ID_CONFIG, AppConfigs.applicationID);
producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfigs.bootstrapServers);
producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

KafkaProducer<String, PosInvoice> producer = new KafkaProducer<>(producerProps);
```
Now, we can use this producer to send the invoices. Then create a new Producer Record.


The first argument is the topic name. Then the message key. lets  set the store id as the key. Then the record value itself. Copy the same thing to the else part and change the topic name.

```java
  while (true) {
            ConsumerRecords<String, PosInvoice> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, PosInvoice> record : records) {
                if (record.value().getDeliveryType().equals("HOME-DELIVERY") &&
                        record.value().getDeliveryAddress().getContactNumber().equals("")) {
                    //Invalid
                    producer.send(new ProducerRecord<>(AppConfigs.invalidTopicName, record.value().getStoreID(),
                            record.value()));
                    logger.info("invalid record - " + record.value().getInvoiceNumber());
                } else {
                    //Valid
                    producer.send(new ProducerRecord<>(AppConfigs.validTopicName, record.value().getStoreID(),
                            record.value()));
                    logger.info("valid record - " + record.value().getInvoiceNumber());
                }
            }
        }
```


Lets quickly summarize whatever we have done here. 

- We defined consumer configurations,
- We created a Kafka consumer using those configs.
- we subscribed to a Kafka topic and finally started polling the brokers. Each poll call will provide a set of invoices,
- we process them and again poll for next set of invoices.

This example is the most basic form of a stream processing application. However, it implements a consume-transform-produce pipeline. In this pipeline, we consume invoices, identify if they are valid or invalid, and produce the valid invoices to a Kafka topic and invalid ones to a different topic.

You can test this application easily.

Launch the [POS simulator](https://github.com/JaimeSolisS/apache-kafka/tree/main/01-Apache_Kafka_for_Beginners/08-Micro_Project/02-pos-simulator-complete)

start Kafka cluster services. Create POS topic. Execute POS simulator.

Come back to  POS validator project. Invoices are ready. Invoices are already being sent to my local Kafka Cluster, and they are waiting there for getting processed.

Create valid and invalid topics. These are the end result topics.  Now, we can start POS validator.

You can see these log entries.

```
[2022-03-01 15:19:02,929] (guru.learningjournal.kafka.examples.PosValidator) - INFO valid record - 6939166 
[2022-03-01 15:19:02,942] (guru.learningjournal.kafka.examples.PosValidator) - INFO valid record - 81833062 
[2022-03-01 15:19:02,945] (guru.learningjournal.kafka.examples.PosValidator) - INFO valid record - 4483164 
[2022-03-01 15:19:02,962] (guru.learningjournal.kafka.examples.PosValidator) - INFO valid record - 30544135 
[2022-03-01 15:19:02,966] (guru.learningjournal.kafka.examples.PosValidator) - INFO valid record - 15132530 
[2022-03-01 15:19:02,966] (guru.learningjournal.kafka.examples.PosValidator) - INFO valid record - 66262031 
```

Processing got started. Now, we can start a console consumer to check my invalid invoice topic.

```
{"InvoiceNumber":"58485577","CreatedTime":1646173139860,"StoreID":"STR5864","PosID":"POS872","CashierID":"OAS287","CustomerType":"PRIME","CustomerCardNo":"7589671731","TotalAmount":3418.0,"NumberOfItems":2,"PaymentMethod":"CASH","TaxableAmount":3418.0,"CGST":85.45,"SGST":85.45,"CESS":4.2725,"DeliveryType":"HOME-DELIVERY","DeliveryAddress":{"AddressLine":"7418 Dolor St.","City":"Nagpur","State":"Maharastra","PinCode":"710782","ContactNumber":""},"InvoiceLineItems":[{"ItemCode":"628","ItemDescription":"Window Scarf","ItemPrice":1774.0,"ItemQty":1,"TotalValue":1774.0},{"ItemCode":"458","ItemDescription":"Wine glass","ItemPrice":1644.0,"ItemQty":1,"TotalValue":1644.0}]}
{"InvoiceNumber":"24612612","CreatedTime":1646173160126,"StoreID":"STR5646","PosID":"POS639","CashierID":"OAS167","CustomerType":"PRIME","CustomerCardNo":"9711257112","TotalAmount":3511.0,"NumberOfItems":2,"PaymentMethod":"CARD","TaxableAmount":3511.0,"CGST":87.775,"SGST":87.775,"CESS":4.38875,"DeliveryType":"HOME-DELIVERY","DeliveryAddress":{"AddressLine":"HN. 535, 4472 Eu St.","City":"Katihar","State":"Bihar","PinCode":"867820","ContactNumber":""},"InvoiceLineItems":[{"ItemCode":"348","ItemDescription":"Navy chair","ItemPrice":1998.0,"ItemQty":1,"TotalValue":1998.0},{"ItemCode":"253","ItemDescription":"Bathroom cabinet","ItemPrice":1513.0,"ItemQty":1,"TotalValue":1513.0}]}

```

We have got some invalid invoices here. While the example is working perfectly fine, and at first sight, we do not see any issues in this example. However, there is a lot that goes inside the Kafka consumer and creating a real-time stream processing application requires a lot many other considerations, including scalability and fault tolerance.

