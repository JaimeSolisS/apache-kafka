# BankBalance Practice

1. `Create a Kafka Producer` that outputs ~100 messages per seconds to a topic. Each message is random in money (a positive value), and outputs evenly transactions for 6 customers. The data should look like this:
    ```
    { "Name":"John", "amount": 123, "time": "2017-07-19T05:24:52" }
    ```
2. `Create a Kafka Streams application` that takes these transactions and will compute the total money in their balance (the balance starts at $0), and the latest time an update was received.
As you guessed, the total money is not idempotent (sum), but latest time is (max).

3. Run the producer and Streams applications
4. There you go, you have an exactly once pipeline!