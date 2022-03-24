kafka-topics.bat --bootstrap-server localhost:9092 --topic bank-balance-exactly-once --create --partitions 1 --replication-factor 1 ^
    --config cleanup.policy=compact