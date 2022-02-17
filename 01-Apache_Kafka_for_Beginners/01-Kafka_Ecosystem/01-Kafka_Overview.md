# Apache Kafka Overview 
## What is Apache Kafka?

If you look at the Apache Kafka documentation, it says *Apache Kafka is a distributed streaming platform*. It means two things.
- You can use Apache Kafka to create one or more real-time streams of data. 
- And you can use Apache Kafka to process these streams and produce results in real time.

## How does it work?

Kafka adopted Pub/Sub messaging system architecture, and it works as an enterprise messaging system.

A typical messaging system has got three components. **Producer**, **Message broker** And **Consumer**.

>- The producer is a client application that sends data records. These data records are called messages.
>
>- The broker is responsible for receiving messages from the producers and storing them into local storage.
>
>- Finally, the consumers are again client applications that read messages from the broker and process them.

In this architecture, the broker is in the center and acts as a middleman between producers and consumers.

![Apache Kafka Architecture](https://www.cloudkarafka.com/img/blog/kafka-broker-beginner.png)

Kafka works as a Pub/Sub messaging system, where we create producer applications to send data as a stream. We install and configure Kafka server to act as a message broker. And finally, we create consumer applications to process the data stream in real-time.

## How it evolved from a data integration solution to a streaming platform?

Kafka initially started with two things. 

- Server software that you can install and configure to work as a message broker. 

- A Java-based client API library to help with the following:
    - Create Kafka producer applications 
    - Create Kafka consumer applications. 
    
But later, Kafka aspired to become a full fledged real-time streaming platform. And to achieve that objective, they augmented Kafka with three more components

1. Kafka Connect
2. Kafka Streams
3. KSQL

From 2011 to 2019 Kafka evolved as a set of five components:

>- **Kafka Broker** - which is the central server system
>- **Kafka Client API** - which is Producer and Consumer APIs. 
>- **Kafka Connect** - which addresses the initial data integration problem for which Kafka was initially designed.
>- **Kafka streams** -this one is another library for creating real time extreme processing applications.
>- **KSQL** - with KSQL Kafka is now aiming to become a real time database and capture some market sharing Databases and DW/BI space.

## Where does Kakfa fit into an enterprise application ecosystem?

![Architecture](https://docs.confluent.io/5.5.1/_images/kafka-apis.png)

Kafka occupies a central place in your real time data integration infrastructure. The data producers can send data like messages, and they can send it to Kafka brokers as quickly as the business event occurs.

Data consumers can consume the messages from the broker as soon as the data arrives at the broker. With careful design, the messages can reach from producers to consumers in milliseconds.

The producers and consumers are **completely decoupled**, and they do not need tight coupling or direct connections. They always interact with the Kafka broker using a consistent interface.

Producers `do not need to be concerned about who is using the data`, and `they just send the data once without worrying about how many consumers would be reading it`. Producers and consumers can be added, removed, and modified as the business case evolves.