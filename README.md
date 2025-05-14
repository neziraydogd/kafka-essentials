# Comprehensive Kafka Technical Guide


## Kafka Consumer and Producer Config for Production

### Producer Configuration

```java
Properties producerProps = new Properties();
// Essential configs
producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "broker1:9092,broker2:9092,broker3:9092");
producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

// Performance and reliability
producerProps.put(ProducerConfig.ACKS_CONFIG, "all"); // Strongest durability guarantee
producerProps.put(ProducerConfig.RETRIES_CONFIG, 3);
producerProps.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 100);
producerProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384); // Batch size in bytes
producerProps.put(ProducerConfig.LINGER_MS_CONFIG, 5); // Small delay to allow batching
producerProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432); // 32MB buffer

// Idempotence (exactly-once semantics)
producerProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
```

### Consumer Configuration

```java
Properties consumerProps = new Properties();
// Essential configs
consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "broker1:9092,broker2:9092,broker3:9092");
consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "my-consumer-group");
consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

// Performance and reliability
consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // Manual commit for better control
consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // Start from earliest or latest
consumerProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500); // Max records per poll
consumerProps.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000); // 5 minutes
consumerProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000); // 30 seconds
consumerProps.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 10000); // 10 seconds
```

## Dead Letter Queue (DLQ) and Error Handling

Dead Letter Queues provide a mechanism to handle messages that cannot be processed successfully:

```java
@Bean
public KafkaListenerContainerFactory<?> kafkaListenerContainerFactory(
        ConsumerFactory<String, String> consumerFactory,
        KafkaTemplate<String, String> kafkaTemplate) {
    
    ConcurrentKafkaListenerContainerFactory<String, String> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory);
    
    // Configure DLQ error handler
    SeekToCurrentErrorHandler errorHandler = 
            new SeekToCurrentErrorHandler(
                new DeadLetterPublishingRecoverer(kafkaTemplate, 
                    (record, exception) -> new TopicPartition("my-topic-dlq", record.partition())), 
                new FixedBackOff(1000L, 2)); // Retry twice with 1s interval
    
    factory.setErrorHandler(errorHandler);
    return factory;
}
```

Key implementation aspects:
1. **Creation of a DLQ topic**: Separate topic for failed messages
2. **Error classification**: Determining which errors go to DLQ
3. **Original metadata preservation**: Including headers with error information
4. **Monitoring and alerting**: On DLQ traffic
5. **Recovery process**: Manual or automated reprocessing of DLQ messages

## Retry Logic

### Non-retryable exceptions
Exceptions that should immediately go to DLQ without retry:
- Data validation exceptions
- Deserialization errors
- Authorization failures
- Business rule violations

```java
ErrorHandlingDeserializer<String> valueDeserializer = 
    new ErrorHandlingDeserializer<>(new StringDeserializer());
valueDeserializer.addTrustedPackages("*");
valueDeserializer.setFailedDeserializationFunction(failedData -> {
    // Send to DLQ directly
    kafkaTemplate.send("my-topic-dlq", "Deserialization failed: " + new String(failedData));
    return "FAILED_DESERIALIZATION";
});
```

### Retryable exceptions with exponential or fixed backoff

Examples of retryable exceptions:
- Network timeouts
- Temporary database unavailability
- Rate limiting/throttling errors

#### Spring Kafka implementation:

```java
@Bean
public DefaultErrorHandler errorHandler(KafkaTemplate<String, String> template) {
    // Define which exceptions are fatal (non-retryable)
    var exceptionsToIgnore = List.of(
        ValidationException.class,
        IllegalArgumentException.class
    );
    
    // Exponential backoff: 1s, 2s, 4s, 8s, 16s
    ExponentialBackOffWithMaxRetries expBackOff = new ExponentialBackOffWithMaxRetries(5);
    expBackOff.setInitialInterval(1000);
    expBackOff.setMultiplier(2.0);
    expBackOff.setMaxInterval(16000);
    
    var errorHandler = new DefaultErrorHandler(
        new DeadLetterPublishingRecoverer(template, 
            (rec, ex) -> new TopicPartition("my-topic-dlq", rec.partition())),
        expBackOff);
    
    exceptionsToIgnore.forEach(errorHandler::addNotRetryableExceptions);
    return errorHandler;
}
```

## Kafka Producer: Async vs Sync

### Asynchronous Send with CompletableFuture

```java
public CompletableFuture<SendResult<String, String>> sendMessageAsync(String topic, String key, String value) {
    ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);
    
    CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(record)
        .completable()
        .whenComplete((result, ex) -> {
            if (ex == null) {
                // Success
                logger.info("Message sent successfully to topic {} partition {}: {}",
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getProducerRecord().value());
            } else {
                // Failure
                logger.error("Unable to send message to topic {}: {}", topic, ex.getMessage());
            }
        });
    
    return future;
}
```

Benefits:
- Higher throughput
- Non-blocking I/O
- Better resource utilization

### Synchronous Send Example

```java
public SendResult<String, String> sendMessageSync(String topic, String key, String value) {
    ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);
    
    try {
        // Blocks until the send is complete
        SendResult<String, String> result = kafkaTemplate.send(record).get();
        logger.info("Message sent successfully to topic {} partition {}: {}",
            result.getRecordMetadata().topic(),
            result.getRecordMetadata().partition(),
            result.getProducerRecord().value());
        return result;
    } catch (InterruptedException | ExecutionException e) {
        logger.error("Error sending message to topic {}: {}", topic, e.getMessage());
        throw new KafkaException("Failed to send message", e);
    }
}
```

Benefits:
- Simpler error handling
- Guaranteed order of execution
- Easier to reason about in transaction scenarios

## Kafka Listener Annotations

### @KafkaListener Configuration Options

```java
@KafkaListener(
    topics = "my-topic",                        // Topic names
    groupId = "my-consumer-group",              // Consumer group ID
    concurrency = "3",                          // Number of consumer threads
    topicPartitions = {
        @TopicPartition(
            topic = "partitioned-topic",
            partitions = {"0", "1"},            // Specific partitions
            partitionOffsets = {
                @PartitionOffset(
                    partition = "2",
                    initialOffset = "0")        // Starting offset
            }
        )
    },
    containerFactory = "kafkaListenerContainerFactory"
)
public void listen(ConsumerRecord<String, String> record) {
    logger.info("Received: {}", record.value());
    // Processing logic
}
```

### Additional Listener Features

#### Batch Listening
```java
@KafkaListener(topics = "batch-topic", groupId = "batch-group")
public void listenBatch(List<ConsumerRecord<String, String>> records) {
    logger.info("Batch received, size: {}", records.size());
    // Process batch
}
```

#### Content-based Routing
```java
@KafkaListener(topics = "routing-topic", groupId = "router-group")
@SendTo("output-topic") // Route processed results to another topic
public String processAndRoute(String message) {
    return "Processed: " + message;
}
```

#### Error Handling
```java
@KafkaListener(topics = "error-topic", groupId = "error-group", 
               errorHandler = "customErrorHandler")
public void listenWithErrorHandler(String message) {
    // Processing that might throw exceptions
}
```

## Send to Specific Partition

```java
public void sendToPartition(String topic, int partition, String key, String value) {
    ProducerRecord<String, String> record = new ProducerRecord<>(topic, partition, key, value);
    kafkaTemplate.send(record)
        .addCallback(
            result -> logger.info("Message sent to partition {}", partition),
            ex -> logger.error("Failed to send to partition {}: {}", partition, ex.getMessage())
        );
}
```

Alternative approach using partition selector:
```java
// Set a custom partitioner
producerProps.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, "com.example.CustomPartitioner");

// Custom partitioner implementation
public class CustomPartitioner implements Partitioner {
    @Override
    public int partition(String topic, Object key, byte[] keyBytes, 
                         Object value, byte[] valueBytes, Cluster cluster) {
        // Custom logic to determine partition
        if (key.toString().startsWith("high-priority")) {
            return 0; // High priority messages to partition 0
        } else {
            // Other partitioning logic
            return Math.abs(key.hashCode() % cluster.partitionCountForTopic(topic));
        }
    }
    
    @Override
    public void close() {}
    
    @Override
    public void configure(Map<String, ?> configs) {}
}
```

## Send with Headers

```java
public void sendWithHeaders(String topic, String key, String value, Map<String, String> headers) {
    ProducerRecord<String, String> record = new ProducerRecord<>(topic, null, key, value);
    
    // Add headers
    headers.forEach((headerKey, headerValue) -> 
        record.headers().add(headerKey, headerValue.getBytes(StandardCharsets.UTF_8)));
    
    // Add tracing and metadata headers
    record.headers().add("trace-id", UUID.randomUUID().toString().getBytes());
    record.headers().add("timestamp", String.valueOf(System.currentTimeMillis()).getBytes());
    
    kafkaTemplate.send(record);
}
```

Processing headers in a consumer:
```java
@KafkaListener(topics = "header-topic", groupId = "header-group")
public void processWithHeaders(ConsumerRecord<String, String> record) {
    // Access headers
    Headers headers = record.headers();
    for (Header header : headers) {
        logger.info("Header {} = {}", 
            header.key(), 
            new String(header.value(), StandardCharsets.UTF_8));
    }
    
    // Extract specific header
    Header traceHeader = headers.lastHeader("trace-id");
    String traceId = traceHeader != null ? 
        new String(traceHeader.value(), StandardCharsets.UTF_8) : "unknown";
    
    logger.info("Processing message with trace-id: {}", traceId);
}
```

## Topic Management APIs

```java
@Service
public class KafkaAdminService {
    private final AdminClient adminClient;
    
    public KafkaAdminService(AdminClient adminClient) {
        this.adminClient = adminClient;
    }
    
    public void createTopic(String topicName, int partitions, short replicationFactor, 
                           Map<String, String> configs) {
        NewTopic newTopic = new NewTopic(topicName, partitions, replicationFactor);
        newTopic.configs(configs);
        adminClient.createTopics(Collections.singleton(newTopic))
            .all().whenComplete((v, ex) -> {
                if (ex != null) {
                    throw new RuntimeException("Failed to create topic", ex);
                }
            });
    }
    
    public void deleteTopic(String topicName) {
        adminClient.deleteTopics(Collections.singleton(topicName))
            .all().whenComplete((v, ex) -> {
                if (ex != null) {
                    throw new RuntimeException("Failed to delete topic", ex);
                }
            });
    }
    
    public void updateTopicConfig(String topicName, Map<String, String> configs) {
        ConfigResource resource = new ConfigResource(ConfigResource.Type.TOPIC, topicName);
        
        List<ConfigEntry> configEntries = configs.entrySet().stream()
            .map(entry -> new ConfigEntry(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
        
        Map<ConfigResource, Config> updateConfig = Collections.singletonMap(
            resource, new Config(configEntries));
        
        adminClient.alterConfigs(updateConfig)
            .all().whenComplete((v, ex) -> {
                if (ex != null) {
                    throw new RuntimeException("Failed to update topic config", ex);
                }
            });
    }
    
    public Map<String, TopicDescription> getTopicDetails(String... topicNames) {
        try {
            return adminClient.describeTopics(Arrays.asList(topicNames)).all().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to get topic details", e);
        }
    }
    
    public void increasePartitions(String topicName, int newPartitionCount) {
        Map<String, NewPartitions> newPartitionsMap = Collections.singletonMap(
            topicName, NewPartitions.increaseTo(newPartitionCount));
        
        adminClient.createPartitions(newPartitionsMap)
            .all().whenComplete((v, ex) -> {
                if (ex != null) {
                    throw new RuntimeException("Failed to increase partitions", ex);
                }
            });
    }
    
    public Set<String> listAllTopics() {
        try {
            return adminClient.listTopics().names().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to list topics", e);
        }
    }
}
```

External tools for topic management:
1. **Confluent Control Center**: Web UI for Kafka management
2. **Kafka CLI Tools**:
   ```bash
   # Create topic
   kafka-topics --bootstrap-server localhost:9092 --create --topic my-topic --partitions 4 --replication-factor 3
   
   # List topics
   kafka-topics --bootstrap-server localhost:9092 --list
   
   # Describe topic
   kafka-topics --bootstrap-server localhost:9092 --describe --topic my-topic
   
   # Add partitions
   kafka-topics --bootstrap-server localhost:9092 --alter --topic my-topic --partitions 8
   
   # Update configs
   kafka-configs --bootstrap-server localhost:9092 --alter --entity-type topics --entity-name my-topic --add-config retention.ms=86400000
   ```

3. **AKHQ (Kafka HQ)**: Open-source web UI for Kafka management
4. **Kafdrop**: Simple web UI for viewing Kafka topics and browsing consumer groups

## Acknowledgement Modes

### Available Acknowledgement Modes in Spring Kafka

1. **RECORD**: Commits the offset after each record is processed
2. **BATCH**: Commits after all records returned by the poll are processed
3. **TIME**: Commits based on a time interval
4. **COUNT**: Commits after processing a specified number of records
5. **COUNT_TIME**: Commits when either count or time condition is met
6. **MANUAL**: Manual acknowledgment by calling `Acknowledgment.acknowledge()`
7. **MANUAL_IMMEDIATE**: Immediately commits when acknowledge is called

```java
@Bean
public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
        ConsumerFactory<String, String> consumerFactory) {
    
    ConcurrentKafkaListenerContainerFactory<String, String> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory);
    
    // Configure acknowledgment mode
    factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
    
    return factory;
}

@KafkaListener(topics = "manual-ack-topic", groupId = "manual-group")
public void listenWithManualAck(ConsumerRecord<String, String> record, Acknowledgment ack) {
    try {
        // Process the record
        logger.info("Processing: {}", record.value());
        
        // Acknowledge the message after successful processing
        ack.acknowledge();
    } catch (Exception e) {
        // Handle exception, potentially not acknowledging
        logger.error("Failed to process: {}", e.getMessage());
        // For retry logic, don't acknowledge
    }
}
```

### Kafka Consumer Config: auto.commit.interval.ms

This property defines how frequently the consumer commits offsets automatically when `enable.auto.commit` is set to `true`:

```java
Properties consumerProps = new Properties();
consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
consumerProps.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 5000); // 5 seconds
```

Considerations:
1. **Higher values**:
    - Less frequent commits, better performance
    - Risk of more duplicates after consumer failure
    - Example: 10000ms (10 seconds) for high-throughput, non-critical workloads

2. **Lower values**:
    - More frequent commits, reduced duplicates
    - Higher overhead, potentially lower throughput
    - Example: 1000ms (1 second) for more critical workloads

3. **Best practice**:
    - Disable auto-commit (`enable.auto.commit=false`) for critical workloads
    - Use explicit commits after processing for better control

```java
@KafkaListener(topics = "manual-commit-topic", groupId = "manual-commit-group")
public void listenWithManualCommit(ConsumerRecord<String, String> record, 
                                   Consumer<?, ?> consumer) {
    try {
        // Process the record
        logger.info("Processing: {}", record.value());
        
        // Commit offsets manually
        Map<TopicPartition, OffsetAndMetadata> offsets = Collections.singletonMap(
            new TopicPartition(record.topic(), record.partition()),
            new OffsetAndMetadata(record.offset() + 1));
        
        consumer.commitSync(offsets);
    } catch (Exception e) {
        logger.error("Failed to process: {}", e.getMessage());
    }
}
```