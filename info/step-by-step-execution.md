# Apache Kafka Docker Setup

This project provides a Docker Compose configuration to run Apache Kafka in KRaft mode (without Zookeeper).

## Commands
- docker-compose up -d 
- docker-compose ps 
- docker-compose logs kafka 
- docker exec kafka kafka-topics --bootstrap-server localhost:9092 --create --topic test-topic --partitions 3 --replication-factor 1
- docker exec kafka kafka-topics --bootstrap-server localhost:9092 --list
- docker exec -it kafka kafka-console-producer --bootstrap-server localhost:9092 --topic test-topic
- docker exec -it kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic test-topic --from-beginning
- docker exec kafka kafka-topics --bootstrap-server localhost:9092 --create --topic configured-topic --partitions 3 --replication-factor 1 --config retention.ms=86400000
- docker exec kafka kafka-topics --bootstrap-server localhost:9092 --describe --topic configured-topic
- docker exec -it kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic test-topic --group test-group
- docker exec kafka kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group test-group
- docker exec kafka kafka-consumer-groups --bootstrap-server localhost:9092 --list

## Start
```bash
docker-compose up -d
```
```bash
docker-compose ps
```
### You should see both kafka and kafka-ui containers showing as "Up".
- Kafka	9092, 29092	Single-node broker in KRaft mode
- Kafka UI	8080	Web management interface
### Verify Kafka/KafkaUI is Working Properly

```bash
docker-compose logs kafka
```
- Look for "Kafka Server started" or similar success messages.
- Access the Kafka UI:
  - Open your browser and navigate to http://localhost:8080
  - Verify you can see the Kafka cluster named "local-kraft"
  - The UI should show 0 topics initially (except for internal ones)

## Testing
### 1 ) ## Command Explanation: 
####  Create Topic
```bash 
docker exec kafka kafka-topics --bootstrap-server localhost:9092 --create --topic test-topic --partitions 3 --replication-factor 1
```

This command is used to create a new topic named `test-topic` within your Kafka cluster running inside a Docker container named `kafka`.

-   **`docker exec kafka`**:
  -   `docker exec`: This is a Docker command that allows you to run a command inside a running Docker container.
  -   `kafka`: This specifies the name of the Docker container where your Kafka broker is running. 
               By using `docker exec kafka`, you are telling Docker to execute the subsequent command within the context of this `kafka` container.

-   **`kafka-topics`**:
  -   This is a command-line tool that comes with Kafka. 
      It is used for managing Kafka topics, allowing you to create, list, describe, and delete topics.
-   **`--bootstrap-server localhost:9092`**:
  -   This flag tells the `kafka-topics` command how to connect to your Kafka cluster to perform the topic operation.
  -   `localhost:9092`: This is the address and port of one of your Kafka brokers. The `kafka-topics` tool uses 
                        this initial connection to discover the rest of the brokers in the cluster. In this setup, 
                        since Kafka is running within a Docker container and you are executing the command from 
                        your host machine (via `docker exec`), `localhost` here refers to the network interface accessible 
                        within the `kafka` container. The standard default port for Kafka brokers is `9092`.
-   **`--create`**:
  -   This flag specifies the action you want to perform with the `kafka-topics` tool. 
      In this case, `--create` indicates that you want to create a new Kafka topic.

-   **`--topic test-topic`**:
  -   This flag defines the name of the topic you want to create. Here, you are naming the new topic `test-topic`. 
      Topics are logical groupings of messages in Kafka.

-   **`--partitions 3`**:
  -   This flag sets the number of partitions for the `test-topic`.
  -   Partitions are a way to divide a topic into multiple, ordered message logs. 
      This allows for parallelism in message consumption (multiple consumers can read from different partitions) and 
      can improve the overall throughput of your Kafka cluster. In this case, the `test-topic` will be created with 3 partitions.

-   **`--replication-factor 1`**:
  -   This flag specifies the replication factor for the partitions of the `test-topic`.
  -   The replication factor determines how many copies of each partition will be maintained across different Kafka brokers in the cluster. 
      A replication factor of 1 means that each partition will only exist on one broker, providing no redundancy. 
      For production environments, a replication factor greater than 1 (e.g., 2 or 3) is highly recommended to ensure data 
      durability and fault tolerance. In this example, the replication factor is set to 1, 
      which is suitable for a basic testing environment with a single broker.

####  List the topics
```bash 
docker exec kafka kafka-topics --bootstrap-server localhost:9092 --list
```
-   **`--list`**:
  - This flag tells Kafka to list all the topics that exist in the Kafka cluster. 
  - It will return the names of all topics currently in the Kafka broker specified in the --bootstrap-server flag.

####  Produce messages:
```bash 
docker exec -it kafka kafka-console-producer --bootstrap-server localhost:9092 --topic test-topic
```
-   **`-it`**:
  - These flags enable interactive terminal mode. The -i flag keeps the standard input open, and 
    the -t flag allocates a pseudo-terminal, allowing you to type and interact with the command in the terminal.
-   **`kafka-console-producer`**:
  - This is a Kafka command-line tool that allows you to send (produce) messages to a Kafka topic. 
  - It is used to send messages to a specified Kafka topic from the command line.
-   **`--topic test-topic`**:
  - This flag specifies the Kafka topic to which the messages will be sent. 
  - In this case, you are producing messages to the topic named test-topic.
** Type a few messages and press Ctrl+D to exit.
####  Consume the messages:
```bash 
docker exec -it kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic test-topic --from-beginning
```
-   **`kafka-console-consumer`**:
  - A Kafka CLI tool used to consume (read) messages from a Kafka topic and print them to the terminal.
-   **`--from-beginning`**:
  - This tells the consumer to read all messages from the start of the topic, not just new incoming ones.
** You should see the messages you produced. Press Ctrl+C to exit.

####  Create a topic with custom configurations:
```bash 
docker exec kafka kafka-topics --bootstrap-server localhost:9092 --create --topic configured-topic --partitions 3 --replication-factor 1 --config retention.ms=86400000
```
-   **`--partitions 3`**:
  - Sets the topic to have 3 partitions, which allows for parallel processing and scalability.
-   **`--replication-factor 1`**:
  - Means each partition will have 1 replica (i.e., no redundancy). This is fine for local development but not recommended in production.
-   **`--config retention.ms=86400000`**:
  - This sets a custom configuration for the topic:
  - retention.ms=86400000 = 24 hours in milliseconds. 
  - It tells Kafka to retain messages for 1 day, after which they may be deleted if no consumers need them.
####  Describe the topic:
```bash 
docker exec kafka kafka-topics --bootstrap-server localhost:9092 --describe --topic configured-topic
```
-   **`--describe`**:
    The --describe option in Kafka CLI is used to display details about a topic.
####  Consumer groups
```bash 
docker exec -it kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic test-topic --group test-group
```
 ```bash 
docker exec kafka kafka-consumer-groups --bootstrap-server localhost:9092 --list
```
 ```bash 
docker exec kafka kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group test-group
```
-   **`kafka-console-consumer`**: Kafka's built-in tool to consume messages from a topic.
-   **`--topic test-topic`**:The topic to consume messages from.
-   **`--group test-group`**: Assigns this consumer to the consumer group named test-group.

####  Check the Kafka metadata:
```bash 
docker exec kafka kafka-metadata-quorum --bootstrap-server localhost:9092 status
```

## Scenarios -----------------------------------------------------------------------------------------------------------
### Multiple Partitions and Consumer Groups
- Step 1: Create a Topic with Multiple Partitions
```bash
docker exec kafka kafka-topics --bootstrap-server localhost:9092 --create --topic multi-partition-topic --partitions 3 --replication-factor 1
```
- Step 2: Set Up Multiple Consumers in the Same Group
```bash
docker exec -it kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic multi-partition-topic-test   --consumer-property group.id=demo-group --property print.key=true --property print.value=true --property print.timestamp=true --property print.partition=true --property print.offset=true
```
- Step 3: Produce Messages and Observe Load Distribution
```bash
docker exec -it kafka kafka-console-producer   --bootstrap-server localhost:9092   --topic multi-partition-topic-test   --property "parse.key=true"   --property "key.separator=:"
```
- Each message is hashed to the key-value and goes to the same partition.
    ```
    user:1
    user100:100
    user500:500
    ```
- Result: Type multiple messages and observe how they are distributed across your consumers.
  Since there are 5 partitions and 3 consumers, each consumer should receive messages from specific partitions.
- check:
```bash  
docker exec -it kafka kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group demo-group
```
Each row shows(output):
- Which partition of the topic is being read
- By which consumer (CONSUMER-ID)
- On which host
- And its offset lag
### Testing Partition Rebalancing
1)Start two consumers in the same group:
#### Terminal 1
```bash
docker exec -it kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic multi-partition-topic --group rebalance-group
```
#### Terminal 2
```bash
docker exec -it kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic multi-partition-topic --group rebalance-group
```
2)Check the partition assignment:
```bash
docker exec kafka kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group rebalance-group
```
3)Add a Third Consumer and Observe Rebalancing
#### Terminal 3
```bash
docker exec -it kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic multi-partition-topic --group rebalance-group
```
4)Check the new partition assignment:
#### Terminal 4
```bash
docker exec kafka kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group rebalance-group
```
*** Now close one consumer (Ctrl+C) and check the partition assignment again to see how the partitions are redistributed.

### Testing Retention Policies
- Step 1: Create a Topic with Short Retention //This creates a topic where messages expire after 60 seconds.
```bash 
docker exec kafka kafka-topics --bootstrap-server localhost:9092 --create --topic short-retention-topic --partitions 1 --replication-factor 1 --config retention.ms=60000
```
Step 2: Produce Messages
```bash 
docker exec -it kafka kafka-console-producer --bootstrap-server localhost:9092 --topic short-retention-topic
```
Step 3: Wait and Verify Message Deletion
```bash 
docker exec -it kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic short-retention-topic --from-beginning
```
***Result*** Wait more than 60 seconds, then try to consume again: You should see that the messages are gone after the retention period.

## Check Offset Position
```bash 
kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group <your-consumer-group-name>
```
- This will show:
    Current offset (what the consumer has read)
    Log end offset (latest produced offset in the topic)
    Lag (difference between the two)