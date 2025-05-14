# Apache Kafka Ecosystem Overview

This repository provides an overview of key Apache Kafka concepts, ecosystem components, and common tools used in modern
data streaming platforms.

---

## 📘 What is Apache Kafka?

Apache Kafka is a **distributed event streaming platform** used to build real-time data pipelines and streaming
applications.
It was originally developed at **LinkedIn** and open-sourced in **2011** under the **Apache Software Foundation**.
Apache Kafka is an open-source distributed event streaming platform designed for high-throughput, fault-tolerant,
publish-subscribe messaging.
It's built to handle trillions of events per day.
---

## What is Streaming?

Streaming is a computing paradigm where data is processed continuously as it arrives, rather than in batches. (
Continuous flow of data (events) in real-time)
This enables real-time analytics, monitoring, and reactions to events as they happen.

## 🔄 Core Concepts

| Concept              | Description                                                                                                                                                                                   
|----------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
| **Streaming**        | Continuous, real-time flow of data records (e.g., logs, transactions, sensor data).                                                                                                           
| **Cluster**          | A group of Kafka brokers working together to ensure fault-tolerance and scalability.(provide scalability and redundancy)                                                                      
| **Topic**            | A named data feed or category to which records are published. They're similar to tables in a database or folders in a file system, but optimized for real-time streaming.                     
| **Partition**        | A topic can be divided into partitions to allow parallel processing. They enable parallel processing and horizontal scalability. Each partition is an ordered, immutable sequence of records. 
| **Leader Partition** | Each partition has a leader broker responsible for all reads and writes. Each partition has one leader and multiple replicas.                                                                 
| **Replica**          | A backup copy of a partition stored on a different broker for high availability. If a broker fails, another replica can take over.                                                            
| **Producer**         | An application that publishes messages to Kafka topics.                                                                                                                                       
| **Consumer**         | An application that subscribes to and reads messages from topics.                                                                                                                             
| **Offset**           | A unique ID assigned to each record in a partition. Tracks message position.                                                                                                                  

- **Consumer Offset**: Tracks the position of the consumer in a partition, indicating which messages have been
  processed.
- **Producer Offset**: Refers to the position in a partition where a producer writes new messages.

---

## ⚙️ Coordination & Management / Ecosystem Components

- **Zookeeper** was traditionally used for coordinating Kafka clusters, managing broker metadata, leader election, and
  configuration.
  It maintains the state of the Kafka cluster.
- **Kafka Raft (KRaft)** is the new consensus protocol replacing Zookeeper in recent versions of Kafka, enabling Kafka
  to
  manage its own metadata internally without requiring Zookeeper.

| Component                      | Description                                                  | License                     |
|--------------------------------|--------------------------------------------------------------|-----------------------------|
| `cp-kafka`                     | Kafka broker for publishing and subscribing to messages      | Apache 2.0                  |
| `cp-zookeeper`                 | Coordinates broker metadata (legacy, replaced by KRaft)      | Apache 2.0                  |
| `cp-schema-registry`           | Manages schemas for data validation and compatibility        | Confluent Community License |
| `cp-kafka-connect`             | Tool to integrate Kafka with external systems via connectors | Confluent Community License |
| `cp-ksqldb-server`             | SQL-based stream processing engine for Kafka                 | Confluent Community License |
| `cp-enterprise-control-center` | GUI for monitoring and managing Kafka clusters               | Limited free features       |

---

## 🚀 Kafka Benefits

- High throughput and low latency (Handles millions of messages per second)
- Fault-tolerant and horizontally scalable
- Real-time stream processing
- Durable and persistent logs(Persists messages to disk)
- Rich ecosystem for integration and analytics

---

## 🚀What Kafka Provides

- Real-time data pipelines
- Stream processing capabilities
- Event sourcing architecture(Stores a log of all events.)
- System decoupling
- Data integration framework (Connects databases, apps, and microservices.)
- Reliable message delivery
- Pub/Sub Messaging: Decouples producers and consumers.

## 🧰 Other Message Brokers

| Broker             | Notes/Key Difference                                                                      
|--------------------|-------------------------------------------------------------------------------------------
| **RabbitMQ**       | Traditional queue-based system (AMQP), Not designed for high-throughput streams           
| **ActiveMQ**       | Java-based message broker,Apache's mature message broker implementation                   
| **Redis Streams**  | Lightweight in-memory streaming,In-memory, no persistence                                 
| **Apache Pulsar**  | Distributed messaging with separate compute/storage layers, Separates storage and compute 
| **Amazon Kinesis** | Managed cloud streaming service                                                           |
| **NATS**           | High-performance messaging system                                                         |

---

## 🌐 Kafka Community

- Open-source contributors under the **Apache Software Foundation**
- Apache Software Foundation maintainers
- Enterprise users like LinkedIn (where Kafka originated), Uber, Netflix, etc.
- Confluent and other commercial vendors
- Independent developers and contributors
- Extensive online forums and regional meetups
  The community continues to actively develop Kafka, with regular releases adding new features and improvements to the
  platform.

---

## 🗓️ Kafka Release History

| Version | Release Date | Key Features                                                                |
|---------|--------------|-----------------------------------------------------------------------------|
| 0.7.0   | Jan 2012     | First stable release under Apache; included basic publish/subscribe system. |
| 0.8.0   | Oct 2013     | Added replication for fault tolerance.                                      |
| 0.9.0   | Nov 2015     | Introduced security (SSL, SASL), consumer groups, and Kafka Connect.        |
| 0.10.0  | May 2016     | Added message timestamps and improved producer APIs.                        |
| 0.11.0  | Jun 2017     | Introduced Exactly-Once Semantics (EOS) and transactions.                   |
| 1.0.0   | Nov 2017     | First "production-ready" version with stability improvements.               |
| 2.0.0   | Jul 2018     | Refactored Kafka Streams API, support for Java 11.                          |
| 2.3.0   | Jun 2019     | Improved replication and KIP-351 (Incremental rebalancing).                 |
| 2.6.0   | Aug 2020     | Added Tiered Storage KIPs, improved Connect features.                       |
| 3.0.0   | Sep 2021     | Dropped support for Scala 2.11, introduced KRaft (early preview).           |
| 3.3.0   | Sep 2022     | Kafka Raft (KRaft) mode production-ready for new clusters.                  |
| 3.5.0   | Jun 2023     | Performance improvements, bug fixes, and minor API enhancements.            |
| 4.0.0   | May 2024     | Removed ZooKeeper entirely, full transition to KRaft mode.                  |

> ℹ️ Starting from version **4.0.0**, Apache Kafka no longer requires ZooKeeper and is fully powered by **KRaft (Kafka
Raft)** metadata management.

## 🕰️ Kafka Release History

### 🔹 Pre-Apache Era (LinkedIn)

- **2010**: Kafka was developed internally at **LinkedIn** by *Jay Kreps*, *Neha Narkhede*, and *Jun Rao* to manage
  real-time data pipelines.
- **2011**: Kafka was open-sourced and entered the **Apache Incubator**.

---

### 🧭 Major Milestones

#### ⚙️ Kafka 1.0 – *2017*

- First production-ready release.
- Enhanced stability and monitoring via the **Metrics API**.

#### ⚙️ Kafka 2.0 – *2018*

- Introduced **incremental cooperative rebalancing** for faster consumer group joins.
- Improved **Kafka Streams API** with better exactly-once semantics.

#### ⚙️ Kafka 2.8 – *2021*

- **KRaft mode (preview)** introduced: the first step toward a ZooKeeper-less Kafka.
- Not yet production-ready at this stage.

#### ⚙️ Kafka 3.0 – *2021*

- Deprecated legacy message formats **v0** and **v1**.
- Improved security defaults (e.g., **PLAINTEXT listeners disabled** by default).

#### ⚙️ Kafka 3.3 – *2022*

- **Production-ready KRaft mode**—Kafka can now run without ZooKeeper.
- Introduced automated **leader balancing** for better self-healing clusters.

#### ⚙️ Kafka 3.4+ – *2023*

- KRaft mode becomes **default**, replacing ZooKeeper in most setups.
- Kafka now supports **up to 2 million partitions per cluster** for massive scalability.

---

### 📌 Kafka 4.0.0 – *2024*

- **ZooKeeper fully removed**—KRaft is the default and only supported metadata mode.
- Marked the **complete transition to ZooKeeper-free architecture**.

## 🛠 Maintainers

- [Apache Kafka](https://kafka.apache.org/)
- [Confluent](https://www.confluent.io/)
- [Bitnami Kafka](https://bitnami.com/stack/kafka/containers)

---

### Confluent/Bitnami:

- Confluent is a company founded by the original creators of Kafka that provides an enterprise platform built around
  Kafka with additional tools and services.
- Confluent Platform (Kafka distribution with tools like Schema Registry, Kafka Connect). Version numbering differs (
  e.g., Confluent 7.4.0 ≈ Kafka 3.4.0).
- Bitnami offers pre-packaged Kafka solutions that simplify deployment across different environments.

# 📘

## 🔍 Broker vs Cluster in Kafka

### 🔹 Broker

* A single Kafka server instance.
* Stores data, serves clients, handles requests.

### 🔹 Cluster

* A group of Kafka brokers.
* Offers scalability and fault tolerance.

| Concept | Broker           | Cluster                                |
|---------|------------------|----------------------------------------|
| Role    | One node/server  | Group of brokers                       |
| Purpose | Store/serve data | Coordinate data and availability       |
| Example | `kafka1:9092`    | Kafka cluster (kafka1, kafka2, kafka3) |

---

## ✅ ACKS Strategies (Producer Acknowledgments)

| Setting    | Description                                         | Reliability | Performance |
|------------|-----------------------------------------------------|-------------|-------------|
| `acks=0`   | Producer does not wait for acknowledgment           | ❌ Low       | ✅ High      |
| `acks=1`   | Wait for leader broker to acknowledge write         | ⚠️ Medium   | ⚠️ Medium   |
| `acks=all` | Wait for all in-sync replicas (ISRs) to acknowledge | ✅ High      | ❌ Lower     |

---

## 🔁 Kafka Streams vs Kafka Connect

### Kafka Streams

- Java library for real-time stream processing.
- Embedded in the application—no need for separate cluster.
- Supports operations like **windowing, joins, aggregations, and transformations**.
- Supports stateful processing with **RocksDB**.

### Kafka Connect

- Tool for scalable **ingestion/export** of data from/to Kafka.
- Uses **source** and **sink connectors**:
    - **Source Examples**: JDBC, MongoDB, Debezium.
    - **Sink Examples**: Elasticsearch, S3, PostgreSQL.
- Supports standalone or distributed mode.

---

## 🛡️ Security Best Practices

### Encryption

- Enable **SSL/TLS** for data-in-transit between brokers, producers, and consumers.

### Authentication

- Use **SASL/PLAIN**, **SCRAM**, or **Kerberos** to authenticate clients and brokers.

### Authorization

- Enable **Access Control Lists (ACLs)** to control access to topics, consumer groups, etc.

### Additional Tips

- Place Kafka behind firewalls or VPCs.
- Regularly rotate keys and certificates.

---

## 📊 Monitoring & Metrics

### Key Tools

- **Prometheus + Grafana**: Metric collection and dashboards.
- **Burrow**: Monitor consumer group lags.
- **JMX Exporter**: Extract JVM and Kafka metrics.
- **Cruise Control**: Cluster balancing and auto-scaling.

### Important Metrics to Monitor

- `UnderReplicatedPartitions`
- `OfflinePartitionsCount`
- `RequestHandlerAvgIdlePercent`
- `BytesInPerSec` / `BytesOutPerSec`
- Consumer lag

---

## ⚙️ Kafka Internals

| Concept        | Description                                   |
|----------------|-----------------------------------------------|
| **Partition**  | Unit of parallelism, append-only log          |
| **Segment**    | Log file chunk for a partition                |
| **Leader**     | Broker that handles all reads/writes          |
| **Follower**   | Brokers that replicate the leader             |
| **ISR**        | In-Sync Replicas – followers up-to-date       |
| **Controller** | Broker responsible for metadata and elections |

---

## 🔄 Changing Topic Configs Dynamically

```bash
# Change retention to 1 hour (3600000 ms)
kafka-configs.sh --bootstrap-server localhost:9092 \
  --entity-type topics --entity-name my-topic \
  --alter --add-config retention.ms=3600000

# Describe current configs
kafka-configs.sh --bootstrap-server localhost:9092 \
  --entity-type topics --entity-name my-topic \
  --describe

```

⏳ Retention Strategies

- **Time-based**: retention.ms -> Deletes messages older than X milliseconds
- **Size-based**: retention.bytes -> Deletes oldest messages when size exceeds X
- **Compaction**: cleanup.policy=compact -> Retains only latest value per key

## Apache Kafka - Advanced Configuration and Concepts

### 🧱 Broker Role:

1. **Data Handling and Storage**
   The broker is the fundamental component of the Kafka cluster. It receives published messages from producers, stores
   them in topic partitions, and serves them to consumers.

2. **Client Communication**
   Brokers accept and process write requests from producers and read requests from consumers.

3. **Partition Management**
   Each broker can act as the **leader** or a **replica** for specific topic partitions. The leader handles all read and
   write operations for its partition, while replicas provide redundancy and failover.

4. **Group Coordination (Legacy ZooKeeper Dependency)**
   In traditional Kafka setups using ZooKeeper, brokers interacted with ZooKeeper to manage consumer groups. In **KRaft
   mode**, this responsibility has largely shifted to **controllers**.

### 🎮 Controller Role:

1. **Metadata Management**
   The controller is the central nervous system of the Kafka cluster. It manages cluster metadata, including:

    * Topics and their partitions
    * Which brokers are leaders and replicas for partitions
    * Active brokers in the cluster
    * Configuration information

2. **Leader Election**
   When a partition leader fails, the controller elects a new leader.

3. **Replication Management**
   The controller assigns replicas for partitions and monitors out-of-sync replicas (ISR - In-Sync Replicas).

4. **Reassignment Handling**
   It manages operations like redistributing topic partitions across brokers.

5. **KRaft Consensus**
   In KRaft mode, controllers use the **Raft consensus algorithm** to agree on metadata changes and leader elections
   among themselves—eliminating the need for an external ZooKeeper ensemble.

# `acks` Parameter and Producer Behavior:

The `acks` parameter on the producer determines how many acknowledgments the producer requires from the brokers
after sending a message. This setting directly influences the delivery guarantees.

* **`acks=0`**:
    * **Delivery Guarantee:** **At-most-once**.
    * **Producer Procedure:** The producer sends the message to the broker without waiting for any acknowledgment.
    * **Outcome:** Fastest and lowest latency, but with the highest potential for data loss if the message doesn't reach
      the broker.
      **Use cases**:
        * Metrics/logging where occasional data loss is acceptable
        * High-volume, non-critical event streaming
        * Real-time analytics with tolerance for data loss
* **`acks=1`**:
    * **Delivery Guarantee:** **At-least-once**.
    * **Producer Procedure:** The producer sends the message to the leader partition and waits for an acknowledgment
      from
      the leader broker that the message has been received and written to its local disk.
    * **Outcome:** More reliable than `acks=0`, as the message is guaranteed to reach the leader. However, data loss is
      possible
      if the leader crashes before replicating to followers. Duplicate messages can occur if the producer retries
      sending after a timeout.
      **Use cases**:
        * Payment processing (with idempotence)
        * Financial transactions (with downstream deduplication)
        * Event sourcing systems
        * Notification Systems (Email/SMS alerts, push notifications.)
* **`acks=all`** (or `-1`):
    * **Delivery Guarantee:** **At-least-once** (approaching **exactly-once** with idempotent producers and
      transactions).
    * **Producer Procedure:** The producer sends the message to the leader and waits for an acknowledgment from the
      leader and all
      in-sync replicas (ISRs) that the message has been received and written to their disks.
    * **Outcome:** Highest reliability, as the message is replicated across multiple brokers.
      Lower throughput and higher latency due to the wait for acknowledgments.
      Duplicate messages can still occur on retries without idempotency or transactions.
      **Use cases**:
        * Critical financial applications
        * Accurate counts and aggregations
        * Stream processing with Kafka Streams or Flink
#  Two Isolation Levels in Kafka
- Kafka supports two isolation modes for consumers:
- **read_uncommitted** (Default)	Reads all messages, even those from aborted transactions.	At-Least-Once (default)
- **read_committed**	Only reads messages from committed transactions (ignores aborted/uncommitted ones).	Exactly-Once (EOS)
## How Kafka Implements read_committed
  - Transaction Coordinator tracks transaction state (commit/abort).
  - Brokers mark messages with transaction metadata.
  - Consumer filters messages based on transaction state:
     - If a transaction is committed → Deliver messages.
     - If a transaction is aborted → Discard messages.
     - If a transaction is in-progress → Wait until resolved.
 - Impact on Consumer Lag
   - With read_committed, consumers may lag if:
   - A long-running transaction is in progress.
   - The producer crashes before committing.
   - Kafka holds uncommitted messages until the transaction resolves (default: transaction.timeout.ms=1 minute)
# Kafka Delivery Semantics in Depth

Kafka Delivery Semantics are the foundational behaviors that define how messages are delivered between producers,
brokers, and consumers, especially in the face of failures, retries, and acknowledgments.

## What is Delivery Semantics?

Delivery semantics describe how many times a message might be delivered to a consumer, depending on failures and
configuration.
Here's a breakdown of at-most-once, at-least-once, and exactly-once delivery guarantees in Kafka, along with
how the `acks` parameter and transactions play a role, from both producer and consumer perspectives.

### Kafka supports three types:

#### At-Most-Once Delivery

1. 🟥 At Most Once Delivery

- Message is delivered zero or one time.
- Message loss can occur, but duplicates never happen
- Highest throughput but lowest reliability
  **Producer to Broker**: The producer sends the message to the broker without waiting for any acknowledgment.(does not
  retry)
    - If broker fails, message is lost
      **Broker to Consumer**: Consumer commits offsets before processing messages.
    - If consumer crashes after committing but before processing, messages are lost(Rarely used in practice)

* Typical Configuration for Producer:

``` properties
  acks=0
  enable.idempotence=false
```

* Typical Configuration for Consumer:

``` properties
  enable.auto.commit=false Disables Kafka’s background auto-committing. Offsets must be committed manually.
  Use consumer.commitSync() (or commitAsync()) to persist the offset to Kafka immediately after reading but before processing.
```

#### At Least Once Delivery

- Messages are guaranteed to be delivered, but possibly more than once. Can lead to duplicate processing
- No data loss, but duplicates may occur
  **Producer to Broker**: Producer sends message and waits for acknowledgment (acks=1 or acks=all).If no acknowledgment
  received, producer retries sending
    - Can create duplicates if broker received message but acknowledgment failed.
      **Producer to Broker**: Consumer commits offsets after processing messages
    - If consumer crashes after processing but before committing, messages will be reprocessed

* Typical Configuration for Producer:

``` properties
    acks=1 or acks=all(acks=-1)
    retries > 0 (retries=Integer.MAX_VALUE)
    enable.idempotence=false
```

* Typical Configuration for Consumer:

``` properties
  enable.auto.commit=false Disables Kafka’s background auto-committing. Offsets must be committed manually.
  Call consumer.commitSync() (or commitAsync()) after processing
```

#### Exactly-Once Delivery

- Messages are guaranteed to be delivered exactly once
- No loss and no duplicates
- More overhead and lower throughput compared to other semantics
- Best for critical applications where duplicate processing is problematic
  **Producer to Broker**:
    - Producer uses idempotent delivery (enable.idempotence=true)
    - Kafka assigns each producer a unique ID and sequence numbers to messages
    - Brokers use these to detect and prevent duplicates
    - Often combined with transactions for end-to-end exactly-once
      **Consumer to Broker**: Consumer uses transactions to atomically process messages and commit offsets
    - Often requires integration with external systems that support transactions
    - Typically implemented using the consumer-producer pattern with transactions

* Typical Configuration for Producer:

``` properties
    enable.idempotence=true // Default: true if `acks=all` and `retries>0`
    acks=all //Ensures messages are committed to all in-sync replicas (ISR).
    transactional.id=some-unique-id
    // initial transaction
    // Send messages
    // Commit the transaction (or abort on failure)
    // producer.abortTransaction();  // Rollback on errors
```

* Typical Configuration for Consumer:

``` properties
    isolation.level=read_committed
    enable.auto.commit=false //Offsets are committed via transactions, not manually.
```

### Exactly-Once and Transactions:

Achieving exactly-once delivery in Kafka requires coordination between producers and consumers, and
transactions are the key mechanism for this:

* **Producer-Side (Transactions for Exactly-Once):**
    1. The producer is configured with a unique `transactional.id` and idempotence (`enable.idempotence=true`) is
       enabled.
    2. The producer begins a transaction (`beginTransaction`).
    3. The producer sends multiple messages to various topics and partitions within the transaction.
    4. Upon successful sending, the producer commits the transaction (`commitTransaction`). If an error occurs, the
       transaction is aborted (`abortTransaction`).
    5. Brokers track the transaction status (pending, commit, abort), and only committed messages are made visible to
       consumers configured for `read_committed` isolation.

* **Consumer-Side (Transactions for Exactly-Once):**
    1. Consumers are configured with `isolation.level=read_committed`. This ensures that consumers only read messages
       from committed
       transactions, effectively ignoring messages from pending or aborted transactions.
    2. In "consume-process-produce" scenarios (often handled by Kafka Streams), the consumption of input messages,
       processing,
       and production of output messages are all performed within the same atomic transaction. The offset commits for
       the input topic are also part of this transaction.

In essence, the `acks` setting primarily governs the producer's delivery guarantee to the broker.
Achieving end-to-end exactly-once semantics requires careful coordination between producers and
consumers using idempotent producers and Kafka's transactional capabilities. The consumer's `isolation.level` is crucial
for ensuring it only reads committed data in transactional scenarios.

## Single Broker with Multiple Partitions

In a Kafka deployment with a single broker but multiple partitions:

- **Benefits**:
    - Enables parallel processing within a single machine
    - Allows for consumer groups with multiple consumers
    - Provides ordering guarantees within each partition

- **Limitations**:
    - No fault tolerance (single point of failure)
    - Limited to the resources of a single machine
    - Cannot fully leverage Kafka's distributed capabilities

- **Technical Implementation**:
  ```java
  Properties props = new Properties();
  props.put("bootstrap.servers", "localhost:9092");
  AdminClient adminClient = AdminClient.create(props);
  
  NewTopic newTopic = new NewTopic("my-topic", 4, (short)1); // 4 partitions, 1 replica
  adminClient.createTopics(Collections.singleton(newTopic));
  ```

## Why Topics Are Not Updated Frequently

Updating Kafka topics can be resource-intensive and potentially disruptive:

1. **Partition Reassignment**: Changes to partitions require data movement across brokers
2. **Availability Concerns**: Topics may be temporarily unavailable during reconfiguration
3. **Rebalancing Overhead**: Consumer groups must rebalance when partitions change
4. **Data Consistency**: Maintaining ordering guarantees becomes complex during changes

Best practice is to plan topic configurations carefully before deployment and avoid frequent changes.

## Topic Configurations: Mutable vs Immutable

### Immutable Configurations
- **Partition count** (can only be increased, never decreased)
- **Topic name**
- **Replication factor** (requires partition reassignment to change)

### Mutable Configurations
- **Retention time/size**: `retention.ms`, `retention.bytes`
- **Maximum message size**: `max.message.bytes`
- **Compression type**: `compression.type`

Example of updating mutable configurations:
```java
ConfigResource resource = new ConfigResource(ConfigResource.Type.TOPIC, "my-topic");
ConfigEntry entry = new ConfigEntry("retention.ms", "604800000"); // 7 days
Config config = new Config(Collections.singleton(entry));
adminClient.alterConfigs(Collections.singletonMap(resource, config));
```
### Cleanup policy
- **Cleanup policy**: `cleanup.policy=delete|compact` 
  - The cleanup policy in Kafka is a topic-level configuration that defines how Kafka handles old or expired data in a topic’s log segments.
  - cleanup.policy=delete
    - Kafka deletes old log segments based on:
      - retention.ms (time-based)
      - retention.bytes (size-based)
  - cleanup.policy=compact -> Keeps only latest value per key	State storage, config tables
      - Kafka retains only the latest record per key. 
      - Uses the message key to identify duplicates. 
      - Messages are compacted in the background. 
      - Use case: Storing the latest state per key, like user profiles, configuration tables
    - cleanup.policy=compact,delete (Both) 
      - You can combine both policies. 
      - Kafka will compact the log and also delete old segments based on retention settings. 
      - Useful when you want both:
        - Latest value per key (compaction)
        - Also remove stale data over time (deletion) 
```yml
      cleanup.policy=compact,delete
      retention.ms=86400000       # 1 day
      segment.ms=3600000          # 1 hour segment
 ```
- A new log segment is created every hour.
- For each key, only the latest value is retained (compaction).
- Segments older than 1 day are deleted if they contain tombstones and the retention time has passed.


