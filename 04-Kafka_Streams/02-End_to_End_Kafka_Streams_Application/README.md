# Kafka Streams Application Terminology

- A **stream** is a sequence of immutable data records, that fully ordered, can be replayed, and is fault tolerant (think of a Kafka Topic as a parallel)
- A **stream processor** is a node in the processor topology (graph). It transforms incoming streams, record by record, and may create a new stream from it.
- A **topology** is a graph of processors chained together by streams

- a `Source processor` is a special processor that takes its data directly from a Kafka Topic. It has no predecessors in a topology, and doesn't transform the data.
- a `Sink processor` is processor that does not have children, it sends the stream data directly to a Kafka topic.

![Topology](https://kafka.apache.org/0102/images/streams-architecture-topology.jpg)