# Testing Replication and Leader Partitions in Kafka (Multi-Broker Cluster)

## Step 1: Start the Multi-Broker Cluster

1. Save the following file as `docker-compose-multi-broker.yml`
2. Start the cluster:

   ```bash
   docker-compose -f docker-compose-multi-broker.yml up -d
   ```
3. Verify all brokers are running:

   ```bash
   docker-compose -f docker-compose-multi-broker.yml ps
   ```

## Step 2: Create a Topic with Replication

```bash
docker exec kafka1 kafka-topics --bootstrap-server kafka1:9092 --create --topic replicated-topic --partitions 3 --replication-factor 3
```

This creates a topic with 3 partitions, each replicated across all 3 brokers.

## Step 3: Verify the Replication and Leader Distribution

```bash
docker exec kafka1 kafka-topics --bootstrap-server kafka1:9092 --describe --topic replicated-topic
```

This will show:

* **Partition leaders** (which broker is the leader for each partition)(Broker currently handling read/write requests for the partition.)
* **Replicas** (brokers that have copies of each partition)
* **In-sync replicas (ISR)** Replicas fully caught up with the leader.

Example output:
```
Topic: replicated-topic   Partition: 0    Leader: 1       Replicas: 1,2,3   Isr: 1,2,3
Topic: replicated-topic   Partition: 1    Leader: 2       Replicas: 2,3,1   Isr: 2,3,1
Topic: replicated-topic   Partition: 2    Leader: 3       Replicas: 3,1,2   Isr: 3,1,2
```

## Step 4: Test Leader Election by Simulating Broker Failure

1. Produce some messages:
   ```bash
   docker exec -it kafka1 kafka-console-producer --bootstrap-server kafka1:9092 --topic replicated-topic
   ```
   Type a few messages and press `Ctrl+D` to exit.

2. Check the leader of partition 0:
   ```bash
   docker exec kafka1 kafka-topics --bootstrap-server kafka1:9092 --describe --topic replicated-topic | grep "Partition: 0"
   ```

3. Stop the leader broker (e.g., broker 1):

   ```bash
   docker stop kafka1
   ```
4. Wait a few seconds and check the new leader from another broker:

   ```bash
   docker exec kafka2 kafka-topics --bootstrap-server kafka2:9092 --describe --topic replicated-topic | grep "Partition: 0"
   ```
5. Consume messages to verify availability:

   ```bash
   docker exec -it kafka2 kafka-console-consumer --bootstrap-server kafka2:9092 --topic replicated-topic --from-beginning
   ```

6. Restart the broker:

   ```bash
   docker start kafka1
   ```

## Step 5: Test Replication During Broker Downtime

1. Stop a broker (not all):

   ```bash
   docker stop kafka3
   ```

2. Produce messages:

   ```bash
   docker exec -it kafka1 kafka-console-producer --bootstrap-server kafka1:9092 --topic replicated-topic
   ```

3. Restart the stopped broker:

   ```bash
   docker start kafka3
   ```

4. Check ISR after a minute:

   ```bash
   docker exec kafka1 kafka-topics --bootstrap-server kafka1:9092 --describe --topic replicated-topic
   ```
* While the broker is down, it's removed from the ISR list. Once it comes back and catches up, 
it reappears in the ISR list. This validates Kafka’s ability to maintain consistency even during node failures.

## Step 6: Controlling Partition Leaders

1. Check current leaders:
   ```bash
   docker exec kafka1 kafka-topics --bootstrap-server kafka1:9092 --describe --topic replicated-topic
   ```

2. Create a reassignment JSON file:

```json
{
  "version": 1,
  "partitions": [
    {"topic": "replicated-topic", "partition": 0, "replicas": [2,1,3], "log_dirs": ["any","any","any"]},
    {"topic": "replicated-topic", "partition": 1, "replicas": [3,2,1], "log_dirs": ["any","any","any"]},
    {"topic": "replicated-topic", "partition": 2, "replicas": [1,3,2], "log_dirs": ["any","any","any"]}
  ]
}
```

3. Save as `reassignment.json` and copy it to the container:

   ```bash
   docker cp reassignment.json kafka1:/tmp/
   ```

4. Execute the reassignment:

   ```bash
   docker exec kafka1 kafka-reassign-partitions --bootstrap-server kafka1:9092 --reassignment-json-file /tmp/reassignment.json --execute
   ```

5. Verify the reassignment:

   ```bash
   docker exec kafka1 kafka-reassign-partitions --bootstrap-server kafka1:9092 --reassignment-json-file /tmp/reassignment.json --verify
   ```

6. Check new leader distribution:

   ```bash
   docker exec kafka1 kafka-topics --bootstrap-server kafka1:9092 --describe --topic replicated-topic
   ```
* After execution, running --describe should show the new leader/replica assignments as defined in the JSON. 
Useful for balancing load manually across the cluster.

## Step 7: Test Preferred Leader Election
1. Create `preferred-leaders.json` file:

```json
{
  "version":1,
  "partitions":[
    {"topic":"replicated-topic","partition":0},
    {"topic":"replicated-topic","partition":1},
    {"topic":"replicated-topic","partition":2}
  ]
}
```

2. Copy the JSON to the container:

   ```bash
   docker cp preferred-leaders.json kafka1:/tmp/
   ```

3. Trigger preferred leader election:

   ```bash
   docker exec kafka1 kafka-leader-election --bootstrap-server kafka1:9092 --election-type PREFERRED --path-to-json-file /tmp/preferred-leaders.json
   ```

4. Verify leaders again:

   ```bash
   docker exec kafka1 kafka-topics --bootstrap-server kafka1:9092 --describe --topic replicated-topic
   ```
* Kafka assigns the first broker in the replica list as the preferred leader. 
This command forces leadership to shift back to the preferred broker if it's available.
* Partition leaders revert to their preferred brokers (the first broker in each replica list). 
This helps ensure a consistent and predictable leadership structure.


### KAFKA_CONTROLLER_QUORUM_VOTERS: "1@controller1:9093,2@controller2:9093,3@controller3:9093"

If three controller nodes are defined (e.g., controller1, controller2, and controller3) with the following configuration, 
and kafka2 (node ID 2) and kafka3 (node ID 3) crash, the following situation will occur:

#### Controller Quorum Status:
- **Remaining Controller:** Only kafka1 (node ID 1) remains operational.
- **Quorum Loss:** Since two out of the three controllers have crashed, the controller quorum is lost. 
For a quorum to remain functional, the majority must be operational. In a three-node quorum, the majority is 2 nodes. 
As only one node remains operational, quorum cannot be achieved.

#### Kafka Cluster Status:
- **Metadata Management Stops:** Since the controller quorum is lost, no changes can be made to the cluster metadata. 
This includes the following operations:
    - **Creating new topics:** Not possible.
    - **Deleting topics:** Not possible.
    - **Changing partition count:** Not possible.
    - **Changing replication factor:** Not possible.
    - **Adding or removing brokers:** Not possible.
    - **Leader election (in case of failure):** If a broker crashes and it was the leader of any partitions, 
        no new leader can be elected because the controller quorum is down.

#### Access to Existing Topics:
- **Producers:** Producers can continue writing to existing topics, but this depends on the broker configuration. 
If brokers can still communicate with their leaders and the ACK level is appropriate, writing may continue.
However, if a leader broker fails and a leader election is required, writing operations will stop because a new leader cannot be elected.
- **Consumers:** Consumers can continue reading messages from existing topics, 
but this is also dependent on the broker and leader statuses. If the consumer can still communicate with the leader broker, 
reading operations will continue. However, if a leader change is required, reading operations will stop. 
Metadata-dependent operations such as creating new consumer groups or managing offsets may also face issues.

#### Broker Status:
- **Operational Broker (kafka1):** The operational broker (kafka1) will maintain its current state since 
it cannot receive new instructions from the controller. New leader elections cannot occur, 
and partition replication operations cannot be managed.
