# Scaling the application

- Our input topic has `2 partitions`, therefore we can launch up to `2
instances of our application in parallel` without any changes in the
code!
- This is because a Kafka Streams application relies on `KafkaConsumer`,
and we saw in the Kafka Basics course that we could add consumers
to a consumer group by just running the same code.
- This makes scaling super easy, without the need of any application
cluster.