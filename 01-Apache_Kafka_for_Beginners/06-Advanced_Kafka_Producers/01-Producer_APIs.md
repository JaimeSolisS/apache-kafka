# Producer APIs

## Horizontal and Vertical Scalability 
Apache Kafka was designed with the scalability in mind, and scaling a Kafka application is straightforward.

If you consider the POS example, then each POS system can create a KafkaProducer object and send the invoices. Multiple POS systems can send invoices in parallel. At the cluster end, it is the Kafka broker that receives the messages and acknowledges the successful receipt of the message. So, if you have hundreds of producers sending messages in parallel, you may want to increase the number of brokers in your Kafka cluster.

A single Kafka broker can handle hundreds of messages or maybe thousands of messages per second. However, you can increase the number of Kafka brokers in your cluster and support hundreds of thousands of messages to be received and acknowledged. On the producer side, you can keep adding the new producers to send the messages to the Kafka server in parallel. This arrangement provides linear scalability by merely adding more producers and brokers. This approach works perfectly for scaling up your overall streaming bandwidth.

However, you also have an opportunity to scale an individual producer using multithreading technique. A single producer thread is good enough to support the use cases where the data is being produced at a reasonable pace. However, some scenarios may require parallelism at the individual producer level as well. You can handle such requirements using multithreaded Kafka producer.

The multithreading scenario may not apply to applications that do not frequently generate new messages.

For example, an individual POS application would be producing an invoice every 2-3 minutes. In that case, a single thread is more than enough to send the invoices to the Kafka cluster. However, if you have an application that generates or receives data at high speed and wants to send it as quickly as possible, you might want to implement a multithreaded application.

## Producer Multi-Threading Scenario 

Let's assume a stock market data provider application. The application receives tick by tick stock data packets from the stock exchange over a TCP/IP socket. The data packets are arriving at high frequency. So, you decided to create a multithreaded data handler.

The main thread listens to the Socket and reads the data packet as they arrive. The main thread immediately handovers the data packet to a different thread for sending the data to the Kafka broker and starts reading the next data packet. The other threads of the application are responsible for uncompressing the packet, reading individual messages from the data packet, validating the message, and sending it further to the Kafka broker.

Similar scenarios are common in many other applications where the data arrives at high speed, and you may need multiple application threads to handle the load. Kafka producer is `thread-safe`. So, your application can share the same producer object across multiple threads and send messages in parallel using the same producer instance.

It is not recommended to create numerous producer objects within the same application instance. Sharing the same producer across the threads will be faster and less resource intensive.