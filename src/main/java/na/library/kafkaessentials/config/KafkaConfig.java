package na.library.kafkaessentials.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.errors.NetworkException;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}") // Kafka broker address from application properties
    private String bootstrapServers;

    @Value("${app.kafka.topic-name}") // Demo topic name
    private String topicName;

    @Value("${app.kafka.custom-topics.notification-topic}") // Notification topic name
    private String notificationTopic;

    @Value("${app.kafka.custom-topics.order-topic}") // Order topic name
    private String orderTopic;

    @Value("${app.kafka.custom-topics.payment-topic}") // Payment topic name
    private String paymentTopic;

    @Value("${app.kafka.retry.max-attempts:5}") // Max retry attempts (default 5)
    private int retryMaxAttempts;

    @Value("${app.kafka.retry.initial-interval:1000}") // Initial interval between retries in ms
    private long retryInitialInterval;

    @Value("${app.kafka.retry.multiplier:2.0}") // Multiplier for exponential backoff
    private double retryMultiplier;

    @Value("${app.kafka.retry.max-interval:64000}") // Maximum interval between retries in ms
    private long retryMaxInterval;

    private static final String DLQ_SUFFIX = ".dlq"; // Suffix for Dead Letter Queue (DLQ) topics
    private static final Logger logger = LoggerFactory.getLogger(KafkaConfig.class); // Logger

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>(); // Kafka admin configuration map
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers); // Set bootstrap servers
        return new KafkaAdmin(configs); // KafkaAdmin bean to manage topics
    }

    @Bean
    public NewTopic demoTopic() {
        Map<String, String> configs = new HashMap<>(); // Topic configurations
        configs.put(TopicConfig.RETENTION_MS_CONFIG, "604800000"); // Retention: 7 days
        configs.put(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_DELETE); // Delete old messages
        configs.put(TopicConfig.COMPRESSION_TYPE_CONFIG, "snappy"); // Use snappy compression
        configs.put(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "2"); // Minimum replicas in sync

        return new NewTopic(topicName, 3, (short) 3) // Create topic with 3 partitions and replication factor 3
                .configs(configs);
    }

    @Bean
    public NewTopic notificationTopic() {
        Map<String, String> configs = new HashMap<>();
        configs.put(TopicConfig.RETENTION_MS_CONFIG, "259200000"); // Retention: 3 days

        return new NewTopic(notificationTopic, 3, (short) 3)
                .configs(configs);
    }

    @Bean
    public NewTopic orderTopic() {
        Map<String, String> configs = new HashMap<>();
        configs.put(TopicConfig.RETENTION_MS_CONFIG, "2592000000"); // Retention: 30 days

        return new NewTopic(orderTopic, 3, (short) 3)
                .configs(configs);
    }

    @Bean
    public NewTopic paymentTopic() {
        Map<String, String> configs = new HashMap<>();
        configs.put(TopicConfig.RETENTION_MS_CONFIG, "7776000000"); // Retention: 90 days
        configs.put(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "2"); // Minimum replicas in sync

        return new NewTopic(paymentTopic, 3, (short) 3)
                .configs(configs);
    }

    // Dead Letter Queue (DLQ) topic for demoTopic
    @Bean
    public NewTopic demoDlqTopic() {
        Map<String, String> configs = new HashMap<>();
        configs.put(TopicConfig.RETENTION_MS_CONFIG, "1209600000"); // Retention: 14 days

        return new NewTopic(topicName + DLQ_SUFFIX, 3, (short) 3)
                .configs(configs);
    }

    @Bean
    public NewTopic notificationDlqTopic() {
        Map<String, String> configs = new HashMap<>();
        configs.put(TopicConfig.RETENTION_MS_CONFIG, "604800000"); // Retention: 7 days

        return new NewTopic(notificationTopic + DLQ_SUFFIX, 3, (short) 3)
                .configs(configs);
    }

    @Bean
    public NewTopic orderDlqTopic() {
        Map<String, String> configs = new HashMap<>();
        configs.put(TopicConfig.RETENTION_MS_CONFIG, "5184000000"); // Retention: 60 days

        return new NewTopic(orderTopic + DLQ_SUFFIX, 3, (short) 3)
                .configs(configs);
    }

    @Bean
    public NewTopic paymentDlqTopic() {
        Map<String, String> configs = new HashMap<>();
        configs.put(TopicConfig.RETENTION_MS_CONFIG, "15552000000"); // Retention: 180 days

        return new NewTopic(paymentTopic + DLQ_SUFFIX, 3, (short) 3)
                .configs(configs);
    }

    // ProducerFactory bean for producing messages (including DLQ)
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers); // Kafka servers
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class); // Serialize keys as strings
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class); // Serialize values as JSON
        configProps.put(ProducerConfig.ACKS_CONFIG, "all"); // Wait for all replicas to ack
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3); // Retry up to 3 times on failure
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory()); // KafkaTemplate for sending messages
    }

    // Listener factory with basic error handling
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory,
            KafkaTemplate<String, Object> kafkaTemplate) {

        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory); // Set consumer factory

        ContainerProperties containerProperties = factory.getContainerProperties();
        containerProperties.setPollTimeout(5000); // Set poll timeout to 5 seconds
        containerProperties.setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE); // Manual immediate ack

        // DLQ publishing recoverer
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, exception) -> {
                    logger.error("Message processing failed. Routing to DLQ. Topic: {}, Exception: {}",
                            record.topic(), exception.getMessage(), exception); // Log error
                    return new org.apache.kafka.common.TopicPartition(
                            record.topic() + DLQ_SUFFIX, record.partition()); // Route to DLQ
                });

        // Use fixed backoff strategy for retries
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 2)); // Retry 2 times with 1s delay

        // Set non-retryable exceptions
        errorHandler.addNotRetryableExceptions(
                IllegalArgumentException.class,
                IllegalStateException.class,
                NullPointerException.class
        );

        // Set retryable exceptions
        errorHandler.addRetryableExceptions(
                org.springframework.messaging.converter.MessageConversionException.class,
                org.springframework.kafka.support.serializer.DeserializationException.class
        );

        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }

    // Listener factory with exponential backoff(1st retry: 1000 ms, 2nd retry: 1000 * 2 = 2000 ms, 3rd retry: 4000 ms)
    //To reduce load on the system and avoid overwhelming a temporarily unhealthy service by retrying too frequently.
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> retryableKafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory,
            KafkaTemplate<String, Object> kafkaTemplate) {

        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);

        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(retryMaxAttempts); // Retry strategy
        backOff.setInitialInterval(retryInitialInterval); // Initial delay
        backOff.setMultiplier(retryMultiplier); //  A number by which the previous delay is multiplied to calculate the next retry interval.
        backOff.setMaxInterval(retryMaxInterval); // Multiplier The maximum wait time between retries.it will not exceed this limit.

        // DLQ recoverer after retries exhausted
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, exception) -> {
                    logger.error("Retry attempts exhausted. Routing to DLQ. Topic: {}, Exception: {}",
                            record.topic(), exception.getMessage(), exception); // Log error
                    return new org.apache.kafka.common.TopicPartition(
                            record.topic() + DLQ_SUFFIX, record.partition()); // Route to DLQ
                });

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff); // Error handler with backoff

        // Set non-retryable exceptions
        errorHandler.addNotRetryableExceptions(
                IllegalArgumentException.class,
                IllegalStateException.class,
                NullPointerException.class
        );

        factory.setCommonErrorHandler(errorHandler); // Apply handler to factory

        return factory;
    }

    // Listener factory for DLQ consumers (no retry)
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> dlqKafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory); // Set consumer factory

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                new FixedBackOff(0L, 0L)); // No retries

        factory.setCommonErrorHandler(errorHandler); // Apply handler

        return factory;
    }

    // Retry template for synchronous operations
    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        FixedBackOffPolicy backOff = new FixedBackOffPolicy();
        backOff.setBackOffPeriod(1000); // Backoff period: 1 second
        retryTemplate.setBackOffPolicy(backOff);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3); // Retry 3 times

        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>(); // Retryable exceptions map
        retryableExceptions.put(TimeoutException.class, true); // Retry on timeout
        retryableExceptions.put(NetworkException.class, true); // Retry on network error
        retryPolicy = new SimpleRetryPolicy(3, retryableExceptions, true);

        retryTemplate.setRetryPolicy(retryPolicy); // Apply policy
        return retryTemplate;
    }
}