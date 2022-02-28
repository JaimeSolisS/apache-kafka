# Creating Point of Sale Simulator

we are going to create a point of sale simulator. The POS simulator is a producer that generates an infinite number of random but realistic invoices and sends it to the Kafka broker. The simulator takes three arguments from the command line.

- Topic name - That tells which topic do you want the producer to send all the invoices.

- Number of Producer threads - That says how many parallel threads do you want to create for this application.

- Produce speed - that tell the number of milliseconds that each thread will wait between two invoices. 

So if you are creating 10 threads and giving a 100 milliseconds sleep time, that means, each thread will send 10 messages in one second, and since you have 10 threads, you will generate 100 messages per second.

The only thing that we are not configuring is the size of each message. Otherwise, this application could be an excellent tool for generating specific workload on your Kafka cluster and perform some load testing and monitoring.

However, this application would not generate random text string but will generate JSON formatted realistic invoices.

Same as earlier examples, we have again created a starter project for you. In this starter project, we have already setup usual Java project things such as dependency pom, log4j2.xml, scripts to start your zookeeper and Kafka services, and script to create Kafka topic.

The starter project also comes with two predefined java packages that I have already created and kept in the starter project. We created them beforehand because these are everyday Java things, and we do not have any Kafka specific code.

We'll explain the packages:

### Type Package

We have three classes in this package that defines the invoice structure. The `LineItem Class` defines the structure of a line item in the Invoice, which includes ItemCode, ItemDescription, Price, quality, and total value.

```java
@JsonPropertyOrder({
    "ItemCode",
    "ItemDescription",
    "ItemPrice",
    "ItemQty",
    "TotalValue"
})
```

The `DeliveryAddress Class` defines the structure of an address, which includes AddressLine, City, State, Pin code, and other similar things.

```java
@JsonPropertyOrder({
    "AddressLine",
    "City",
    "State",
    "PinCode",
    "ContactNumber"
})
```
Finally, we have `PosInvoice` which again a structure that includes some standard fields such as InvoiceNumber, CreatedTime, StoreID and also includes the delivery address as well as an array of line items.
```java
@JsonPropertyOrder({
    "InvoiceNumber",
    "CreatedTime",
    "StoreID",
    "PosID",
    "CashierID",
    "CustomerType",
    "CustomerCardNo",
    "TotalAmount",
    "NumberOfItems",
    "PaymentMethod",
    "TaxableAmount",
    "CGST",
    "SGST",
    "CESS",
    "DeliveryType",
    "DeliveryAddress",
    "InvoiceLineItems"
})
```

So, the types package defines all the necessary objects to represent an Invoice. And the code for all these classes is well annotated using Jackson annotations for usage with Jackson DataBind package.

In fact, we haven't handwritten these classes. We have generated these classes from a schema definition using jsonschema2pojo maven plugin.

### Data Generator Package

The data generator package also comes with three classes.

- AddressGenerator which exposes a getNextAddress method to give you a new random address.
```java
   DeliveryAddress getNextAddress() {
        return addresses[getIndex()];
    }
``

- The ProductGenerator class exposes getNextProduct method, which gives you a random line item for your Invoice.

```java
    LineItem getNextProduct() {
        LineItem lineItem = products[getIndex()];
        lineItem.setItemQty(getQuantity());
        lineItem.setTotalValue(lineItem.getItemPrice() * lineItem.getItemQty());
        return lineItem;
    }
```

We won't be using these two classes directly. Both of these classes are internally used by the third class InvoiceGenerator.

- InvoiceGenerator is the class that your producer threads would use to create random Invoices and simply send it to Kafka.

the stage is set for you. You are expected to create a multithreaded application which creates several producer threads. Each thread will use InvoiceGenerator.getNextInvoice() method and send the given Invoice to Apache Kafka.

Earlier we always sent plain string messages. But now, we have an Invoice which is a reasonably complex Java Object. The invoice object has got several fields, and they are of different data types. Some are String, but others are of Integer, number and Long types. The DeliveryAddress field is a more sophisticated type which in itself is an object. The InvoiceLineItems is an array of objects. So, basically an invoice is not a plain string, but it is a complex document with a predefined structure.

You are supposed to serialize this complex PosInvoice Java Object before you can transmit it over the network. You also need to be sure that the serialized Invoice, once received at the consumer, can be correctly de-serialized back into a PosInvoice Java Object. This is why a Kafka producer needs a serializer configuration.

We have used StringSerializer in earlier examples, but this time, the StringSerializer does not fit our requirement. We need a better alternative.

There are two popular alternatives in the Kafka world:

- JSON Serializer and Deserializer.
- Avro Serializer and Deserializer.

The JSON Serializer is easy to use because JSON serialized objects are represented as strings, and that makes them a convenient option. You can easily cast them to a String and print it on the console or in your logs. The simplicity of JSON makes debugging your data issues quite simple. Hence, they are commonly used and supported by many data integration tools. However, JSON serialized messages are large in size. The JSON format includes field names with each data element. These field names may increase the size of your serialized messages by 2X or more, and ultimately, it causes more delays at the network layer to transmit these messages.

The alternative is the Avro Serializer. The Avro is a binary format, and the serialization is much more compact. So, if you are using Avro serialization, your messages will be shorter over the network, giving you a more substantial network bandwidth.

But in this example, we want to use JSON Serializer. We do not need to implement a JSON serializer because it is a standard thing, and we have already included a JsonSerializer class in your startup project inside serde package. You can use the same in this example, and you are also free to use it in any other application wherever you want to serialize your messages as JSON.

## Hands on 

Change topic-create.cmd to:
```cmd
%KAFKA_HOME%\bin\windows\kafka-topics.bat --create --bootstrap-server localhost:9092 --replication-factor 3 --partitions 3 --topic pos --config min.insync.replicas=2
```

Create AppConfigs class
```java
public class AppConfigs {
    public final static String applicationID = "PosSimulator";
    public final static String bootstrapServers = "localhost:9092,localhost:9093";
}
```

Create a `RunnableProducer` class to create a producer thread. This class implements the Runnable interface. The Runnable interface allows us to execute an instance of this class as a separate Thread.

```java

```

Implement a main() method to create thread and start them in a separate class named `PosSimulator`. 

```java
public class PosSimulator {
    private static final Logger logger = LogManager.getLogger();
    public static void main(String[] args) {
    }
}
```

Create private members and a constructor to take these values in `RunnableProducer`.

```java
 private final AtomicBoolean stopper = new AtomicBoolean(false);
    private KafkaProducer<String, PosInvoice> producer;
    private String topicName;
    private InvoiceGenerator invoiceGenerator;
    private int produceSpeed;
    private int id;

    RunnableProducer(int id, KafkaProducer<String, PosInvoice> producer, String topicName, int produceSpeed) {
        this.id = id;
        this.producer = producer;
        this.topicName = topicName;
        this.produceSpeed = produceSpeed;
        this.invoiceGenerator = InvoiceGenerator.getInstance();
    }
```

Finally, we need to override the run method and implement all the steps that we talked about. The first thing is to generate invoices with `invoiceGenerator`. We need to send these lines to Kafka. so, let's do producer.send(). We provide the topic name, a message key, which would be storeID and the message value (posInvoice object).

```java
 @Override
    public void run(){
        try {
            logger.info("Starting producer thread - " + id);
            while (!stopper.get()) {
                PosInvoice posInvoice = invoiceGenerator.getNextInvoice();
                producer.send(new ProducerRecord<>(topicName, posInvoice.getStoreID(), posInvoice));
                Thread.sleep(produceSpeed);
            }

        } catch (Exception e) {
            logger.error("Exception in Producer thread - " + id);
            throw new RuntimeException(e);
        }
    }

    void shutdown() {
        logger.info("Shutting down producer thread - " + id);
        stopper.set(true);

    }
```
Now lets code PosSimulator. Get topic name, number of producers and speed from command line
```java
        if (args.length < 3) {
            System.out.println("Please provide command line arguments: topicName noOfProducers produceSpeed");
            System.exit(-1);
        }
        String topicName = args[0];
        int noOfProducers = new Integer(args[1]);
        int produceSpeed = new Integer(args[2]);
```
Define a properties object and load id. Put other necessary configurations directly into the properties.

```java
Properties properties = new Properties();
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, AppConfigs.applicationID);
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfigs.bootstrapServers);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
```

Create Kafka producer 
```java
 KafkaProducer<String, PosInvoice> kafkaProducer = new KafkaProducer<>(properties);
```

Create the producer threads

```java
 ExecutorService executor = Executors.newFixedThreadPool(noOfProducers);
        final List<RunnableProducer> runnableProducers = new ArrayList<>();

        for (int i = 0; i < noOfProducers; i++) {
            RunnableProducer runnableProducer = new RunnableProducer(i, kafkaProducer, topicName, produceSpeed);
            runnableProducers.add(runnableProducer);
            executor.submit(runnableProducer);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (RunnableProducer p : runnableProducers) p.shutdown();
            executor.shutdown();
            logger.info("Closing Executor Service");
            try {
                executor.awaitTermination(produceSpeed * 2, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }));
```