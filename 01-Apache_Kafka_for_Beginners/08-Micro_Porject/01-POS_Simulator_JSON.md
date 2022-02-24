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