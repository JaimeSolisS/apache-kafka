# Internal Topics 
- - Running a Kafka Streams may eventually create internal intermediary
topics.
Two types:
   - `Repartitioning topics`: in case you start transforming the key of your stream, a repartitioning will happen at some processor.
   - `Changelog topics`: in case you perform aggregations, Kafka Streams will save
   compacted data in these topics
- Internal topics:
   - Are managed by Kafka Streams
  - Are used by Kafka Streams to save / restore state and re-partition data
  - Are prefixed by application.id parameter
   - **Should never be deleted, altered or published to**. `They are internal`.