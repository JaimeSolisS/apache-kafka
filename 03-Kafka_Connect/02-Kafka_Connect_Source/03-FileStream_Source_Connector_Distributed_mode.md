# Filestream Source Connector Distributed Mode
- Goal:
    - Read a file and load the content directly into Kafka
    - Run in distributed mode on our already set-up Kafka Connect Cluster
- Learning:
    - Understand how to configure a connecto in distributed mode
    - Get a first feel for Kafka Connect Cluster
    - Understand the shchema configuration option

What we'll do is launch our Kafka tools, then create a topic, then in the UI pushin a configuration. 

Launch 
```sh
docker run --rm -it --net=host landoop/fast-data-dev:2.3.0 bash
```

Create a second topic 
```sh
kafka-topics --create --topic demo-2-distributed --partitions 3 --replication-factor 1 --zookeeper 127.0.0.1:2181
```

Go to the UI at 127.0.0.1:3030, you should see that the topic has been created and open **KAFKA CONNECT UI**. Click on New and select File. Use the parameters in file-stream-demo-distributed.properties and paste them in the UI. As the Configurations is valid click on Create. 

So because we're running the connector in distributed mode, we actually have to have that file onto the Kafka cluster.

```sh
docker ps
```
Copy the container ID

```sh
docker exec -it 27c6026e0ad3 bash 
```
Now inside the container we need to create demo-file.txt and push data to it. 
```sh
touch demo-file.txt
echo "hi" >> demo-file.txt
echo "hello" >> demo-file.txt
echo "from the other side" >> demo-file.txt
```

And now what happens is that if we refresh the UI we can see that data has been pushed directly into the topic. Although we see in the UI that data is in JSON format, we can verify it also with a consumer. 

```sh 
docker run --rm -it --net=host landoop/fast-data-dev:2.3.0 bash
kafka-console-consumer --topic demo-2-distributed --from-beginning --bootstrap-server 127.0.0.1:9092
{"schema":{"type":"string","optional":false},"payload":"hi"}
{"schema":{"type":"string","optional":false},"payload":"hello"}
{"schema":{"type":"string","optional":false},"payload":"from the other side"}
```