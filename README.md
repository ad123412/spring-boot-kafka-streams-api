Before running the application please follow the steps below :

- download the kafka binary
- start the zookeeper: bin/zookeeper-server-start.sh config/zookeeper.properties
- start the kafka:  bin/kafka-server-start.sh config/server.properties
- create the input topic: bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic bank-transaction
- create the final output topic: bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic bank-balance-exactly-once
- check the topis list: bin/kafka-topics.sh --list --bootstrap-server localhost:9092