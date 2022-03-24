# Creating User Enrich Kafka Streams application 
- Join User Purchases (KStream) to User Data (GlobalKTable)
- Write a producer to explain the different scenarios
- Observe the output!

## Topology

1. **Read** one topic from Kafka (KStream)
2. **Read** the other topic from Kafka (GlobalKTable)
3. `Inner Join`
4. **Write** to Kafka the result of the inner join
5. `Left Join`
6. **Write** to Kafka the result of the left join