# Topology and operations 
1. **Read** one topic from Kafka (KStream)
2. `Filter` bad values
3. `SelectKey` that will be the user id
4. `MapValues` to extract the color (as lowercase)
5. `Filter` to remove bad colors
6. **Write** to Kafka as intermediary topic
7. **Read** from Kafka as a KTable (KTable)
8. `GroupBy` colors
9. `Count` to count colors occurrences (KTable)
10. **Write** to Kafka as final topic