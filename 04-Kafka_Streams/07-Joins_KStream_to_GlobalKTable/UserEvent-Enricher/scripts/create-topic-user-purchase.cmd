REM create input topic for user purchases
kafka-topics.bat --bootstrap-server localhost:9092 --topic user-purchases --create --partitions 3 --replication-factor 1