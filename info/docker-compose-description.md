# Docker Compose for Kafka with KRaft Mode

## Docker Compose Version

```yaml
version: '3'
```

**Explanation:** Specifies the Docker Compose file format version. Version 3 is the recommended and most widely supported format for current Docker Compose implementations, offering more features and flexibility compared to older versions.

## Services

```yaml
services:
```

**Explanation:** This is the top-level key in the Docker Compose file that defines all the services (which correspond to Docker containers) that will be part of this application stack. Each service definition underneath this key describes how a specific container should be created and run.

### Kafka Service

```yaml
kafka:
```

**Explanation:** Defines a service named kafka. This name will be used as the identifier for this container within the Docker Compose network and in Docker commands (e.g., docker-compose logs kafka).

#### Image

```yaml
image: confluentinc/cp-kafka:7.5.0
```

**Explanation:** Specifies the Docker image that should be used to create the kafka container.

- **confluentinc/cp-kafka:** This refers to the official Kafka image provided by Confluent, the company founded by the creators of Kafka. It includes a fully functional Kafka broker along with other essential components.
- **:7.5.0:** This tag specifies the exact version of the Confluent Platform Kafka image to use, in this case, version 7.5.0. Using a specific version ensures consistency and avoids unexpected behavior from automatic updates to the latest tag.

#### Container Name

```yaml
container_name: kafka
```

**Explanation:** Sets a custom name for the Docker container that will be created from this service. Instead of Docker assigning a random name, the container will be explicitly named kafka. This makes it easier to reference the container in Docker commands, logs, and other configurations.

#### Ports

```yaml
ports:
  - "9092:9092"
  - "9093:9093"
```

**Explanation:** This section maps ports from the host machine to the ports inside the kafka container. This allows external applications and other services running on your host to communicate with the Kafka broker.

- **"9092:9092":** Maps port 9092 on your host machine to port 9092 inside the kafka container. This is the standard port used by Kafka clients to connect to the broker for producing and consuming messages.
- **"9093:9093":** Maps port 9093 on your host machine to port 9093 inside the kafka container. In this KRaft mode configuration, port 9093 is used for communication between the Kafka controller and the broker.

#### Environment Variables

```yaml
environment:
  # KRaft mode settings
  KAFKA_NODE_ID: 1
```

**Explanation:** Sets an environment variable named KAFKA_NODE_ID inside the kafka container and assigns it the value 1. In KRaft mode, each Kafka broker and controller needs a unique ID within the cluster. This setting identifies this specific Kafka instance as node ID 1.

```yaml
KAFKA_PROCESS_ROLES: "broker,controller"
```

**Explanation:** Sets the KAFKA_PROCESS_ROLES environment variable to "broker,controller". This is a crucial setting for KRaft mode. It configures this single Kafka instance to act as both a broker (handling client requests for reading and writing data) and a controller (managing the cluster metadata, such as topic partitions and leader elections). In a larger, more resilient setup, you would typically have separate controller nodes.

```yaml
KAFKA_CONTROLLER_QUORUM_VOTERS: "1@kafka:9093"
```

**Explanation:** Defines the list of controller quorum voters for the Kafka cluster. 
In KRaft mode, controllers form a quorum to make decisions.

- **"1@kafka:9093":** Specifies a single controller voter.
	- **1:** This is the KAFKA_NODE_ID of the controller.
	- **@kafka:** This is the hostname or service name of the Kafka container within the Docker network. Docker Compose automatically resolves service names to their respective container IPs.
	- **:9093:** This is the port used for controller communication, as defined by KAFKA_CONTROLLER_LISTENER_NAMES and KAFKA_LISTENERS.
- In KRaft (Kafka Raft) mode, the KAFKA_CONTROLLER_QUORUM_VOTERS parameter defines the set of servers (voters) that participate 
in the controller quorum - the decision-making group responsible for critical cluster operations like:
Leader elections, Metadata management (topics, partitions, configurations), Cluster coordination
Form a Raft consensus group to manage cluster metadata, Elect a leader controller (active controller), 
- Replicate metadata changes across the cluster

```yaml
KAFKA_CONTROLLER_LISTENER_NAMES: "CONTROLLER"
```

**Explanation:** Sets the KAFKA_CONTROLLER_LISTENER_NAMES environment variable to "CONTROLLER".
This defines a logical name for the listener that the controller will use for inter-controller and controller-broker communication. 
This name is then referenced in KAFKA_LISTENERS and other related configurations.

```yaml
KAFKA_LISTENERS: "PLAINTEXT://0.0.0.0:9092,CONTROLLER://0.0.0.0:9093"
```

**Explanation:** Defines the network listeners that the Kafka broker/controller will bind to.

- **"PLAINTEXT://0.0.0.0:9092":** Creates a listener named PLAINTEXT that listens on all network interfaces (0.0.0.0) on port 9092. 
This listener is typically used for client connections (producers and consumers). PLAINTEXT indicates unencrypted communication.
- **"CONTROLLER://0.0.0.0:9093":** Creates a listener named CONTROLLER that listens on all network interfaces on port 9093. 
This listener is used for internal communication between the controller and brokers. 
"CONTROLLER" references the logical name from KAFKA_CONTROLLER_LISTENER_NAMES

```yaml
KAFKA_ADVERTISED_LISTENERS: "PLAINTEXT://kafka:9092"
```

**Explanation:** Specifies the addresses that the Kafka broker will advertise to clients so they can connect.

- **"PLAINTEXT://kafka:9092":** Advertises the PLAINTEXT listener with the hostname kafka
(the Docker service name, which resolves to the container's IP within the app-net network) and port 9092. 
This is important because clients running outside the Docker network need an address they can resolve to reach the Kafka broker.
- **KAFKA_LISTENERS**  "I'm listening for connections at these addresses and ports." Defines the broker's internal binding points
- **KAFKA_ADVERTISED_LISTENERS** "Connect to me using this address and port." The external access point advertised to clients and other brokers

```yaml
KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: "CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT"
```

**Explanation:** Maps each listener name to its corresponding security protocol.

- **"CONTROLLER:PLAINTEXT":** Specifies that the CONTROLLER listener uses the PLAINTEXT (unencrypted) protocol.
- **"PLAINTEXT:PLAINTEXT":** Specifies that the PLAINTEXT listener also uses the PLAINTEXT (unencrypted) protocol. In a production environment, you would likely use protocols like SASL_SSL or SSL for secure communication.

```yaml
KAFKA_INTER_BROKER_LISTENER_NAME: "PLAINTEXT"
```

**Explanation:** Indicates which listener should be used for communication between different brokers in the Kafka cluster. In this single-node KRaft setup where the same process acts as both broker and controller, this setting still needs to be defined and is set to PLAINTEXT, aligning with the PLAINTEXT listener defined in KAFKA_LISTENERS.

```yaml
# Broker settings
KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
```

**Explanation:** Sets the replication factor for the internal __consumer_offsets topic to 1. This topic stores the offsets of consumer groups. A replication factor of 1 means there is only one copy of this topic's partitions, which is acceptable for a single-node development environment but not recommended for production due to potential data loss if the broker fails.

```yaml
KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS: 0
```

**Explanation:** Configures the delay (in milliseconds) before the initial consumer group rebalance. Setting it to 0 reduces the delay, which can speed up the initial formation of consumer groups when they start. This is often useful in development environments.

```yaml
KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
```

**Explanation:** Sets the minimum number of in-sync replicas (ISRs) required for the transaction state log topic to 1. This topic is used for Kafka's transactional features. An ISR is a replica that is currently synchronized with the leader partition. Setting this to 1 is appropriate for a single-node setup.

```yaml
KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
```

**Explanation:** Sets the replication factor for the transaction state log topic to 1. Similar to the __consumer_offsets topic, a replication factor of 1 means no redundancy for the transaction logs, suitable only for development.

```yaml
# Required for KRaft mode
KAFKA_AUTO_CREATE_TOPICS_ENABLE: "true"
```

**Explanation:** Sets the KAFKA_AUTO_CREATE_TOPICS_ENABLE environment variable to "true". This allows Kafka to automatically create topics when they are first published to or when a consumer tries to subscribe to a topic that doesn't exist. While convenient for development, it's often disabled in production to have more explicit control over topic creation.

```yaml
CLUSTER_ID: "MkU3OEVBNTcwNTJENDM2Qk"
```

**Explanation:** Sets a unique identifier for the Kafka cluster. This is a mandatory setting when running Kafka in KRaft mode. The CLUSTER_ID is a base64-encoded UUID that distinguishes this Kafka cluster from any others. You would typically generate a UUID and then base64 encode it to get this value.

#### Volumes

```yaml
volumes:
  - kafka-data:/var/lib/kafka/data
```

**Explanation:** Defines and mounts a Docker volume to persist the Kafka data.

- **kafka-data:** This is a named Docker volume defined in the volumes section at the end of the docker-compose.yml file. Docker manages the storage of named volumes.
- **:/var/lib/kafka/data:** This is the path inside the kafka container where Kafka stores its data, including topic logs. By mounting the kafka-data volume to this path, the Kafka data will be persisted on the host machine even if the kafka container is stopped, restarted, or removed.

#### Networks

```yaml
networks:
  - app-net
```

**Explanation:** Connects the kafka container to a Docker network named app-net. This allows the kafka service to communicate with other services on the same network (like kafka-ui) using their service names as hostnames.

#### Command

```yaml
command:
  - sh
  - -c
  - |
    echo "Waiting for Kafka to be ready..."
    echo "Creating storage directory format with CLUSTER_ID=$${CLUSTER_ID}"
    kafka-storage format -t $${CLUSTER_ID} -c /etc/confluent/kafka/server.properties
    exec /etc/confluent/docker/run
```

**Explanation:** Overrides the default command that would be executed when the kafka container starts. This custom command performs the necessary steps to initialize the Kafka storage for KRaft mode before starting the Kafka server.

- **sh, -c, |:** This sets up a shell command to be executed. The | allows for a multi-line command.
- **echo "Waiting for Kafka to be ready...":** Prints a message to the container logs.
- **echo "Creating storage directory format with CLUSTER_ID=$${CLUSTER_ID}":** Prints the cluster ID that will be used for formatting. Note the double $$ to escape the variable for Docker Compose.
- **kafka-storage format -t $${CLUSTER_ID} -c /etc/confluent/kafka/server.properties:** This is the crucial command for KRaft mode. It formats the Kafka storage directory using the specified CLUSTER_ID and the server configuration file. This step initializes the metadata log.
	- **kafka-storage format:** The Kafka storage formatting tool.
	- **-t $${CLUSTER_ID}:** Specifies the cluster ID to use for formatting.
	- **-c /etc/confluent/kafka/server.properties:** Specifies the path to the Kafka server configuration file inside the container.
- **exec /etc/confluent/docker/run:** After the storage is formatted, this command executes the default entrypoint script provided by the Confluent Kafka Docker image, which starts the Kafka broker and controller processes. The exec command replaces the current shell process with the execution of the entrypoint script.

### Kafka UI Service

```yaml
# Kafka UI for management
kafka-ui:
```

**Explanation:** Defines a service named kafka-ui for the web-based Kafka management interface.

#### Image

```yaml
image: provectuslabs/kafka-ui:latest
```

**Explanation:** Specifies the Docker image to use for the Kafka UI service.

- **provectuslabs/kafka-ui:** This is a popular open-source Kafka UI image.
- **:latest:** This tag indicates that Docker should use the latest available version of the provectuslabs/kafka-ui image from Docker Hub. While convenient for development, it's generally recommended to use a specific version tag in production for stability.

#### Container Name

```yaml
container_name: kafka-ui
```

**Explanation:** Sets a custom name for the Docker container that will be created for the Kafka UI service, making it easier to manage and reference.

#### Dependencies

```yaml
depends_on:
  - kafka
```

**Explanation:** Specifies that the kafka-ui service should only start after the kafka service has been successfully started. This ensures that the Kafka broker is running and accessible before the UI tries to connect to it.

#### Ports

```yaml
ports:
  - "8080:8080"
```

**Explanation:** Maps port 8080 on your host machine to port 8080 inside the kafka-ui container. This allows you to access the Kafka UI web interface by navigating to http://localhost:8080 in your web browser.

#### Environment Variables

```yaml
environment:
  KAFKA_CLUSTERS_0_NAME: local-kraft
  KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: kafka:9092
```

**Explanation:** Sets environment variables for the kafka-ui container to configure how it connects to the Kafka cluster.

- **KAFKA_CLUSTERS_0_NAME: local-kraft:** Defines the name of the Kafka cluster as it will be displayed in the Kafka UI. Here, it's set to local-kraft. The _0 indicates that this is the first (and in this case, only) Kafka cluster being configured.
- **KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: kafka:9092:** Specifies the bootstrap servers that the Kafka UI will use to connect to the Kafka cluster. kafka:9092 refers to the kafka service name (which resolves to the Kafka container's IP within the app-net network) and the client communication port 9092.

#### Networks

```yaml
networks:
  - app-net
```

**Explanation:** Connects the kafka-ui container to the same app-net Docker network as the kafka container. This allows the Kafka UI to communicate with the Kafka broker using its service name (kafka).

## Network Configuration

```yaml
networks:
  app-net:
    driver: bridge
```

**Explanation:** Defines a custom Docker network named app-net.

- **driver: bridge:** Specifies that this network is a bridge network, which is the default network driver in Docker. Bridge networks allow containers connected to the same network to communicate with each other.

## Volume Configuration

```yaml
volumes:
  kafka-data:
```

**Explanation:** Defines a named Docker volume called kafka-data. Named volumes are managed by Docker and persist 
data across container restarts. This volume is mounted to the /var/lib/kafka/data directory in the kafka container 
to ensure that the Kafka data is not lost when the container is stopped or removed. S
ince no driver or other options are specified, Docker will use the default local driver for this volume.


### Step 6: KAFKA_CONTROLLER_QUORUM_VOTERS: "1@controller1:9093,2@controller2:9093,3@controller3:9093"

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