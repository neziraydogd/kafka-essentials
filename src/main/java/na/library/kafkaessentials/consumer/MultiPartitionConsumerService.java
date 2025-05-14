package na.library.kafkaessentials.consumer;

import lombok.extern.slf4j.Slf4j;
import na.library.kafkaessentials.model.Message;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.core.env.Environment;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Service
@Slf4j
public class MultiPartitionConsumerService {

    private final String applicationInstance;

    public MultiPartitionConsumerService(Environment environment) {
        this.applicationInstance = "Instance-" + Math.abs(environment.hashCode() % 100);
    }

    @KafkaListener(topics = "${app.kafka.topic-name}", groupId = "${app.kafka.group-id}-parallel", concurrency = "2", containerFactory = "kafkaListenerContainerFactory")
    public void consumeFromMultiplePartitions(ConsumerRecord<String, Message> record, @Header(KafkaHeaders.RECEIVED_PARTITION) int partition, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic, Acknowledgment ack) {
        processRecord(record, partition, "General", ack);
    }

    @KafkaListener(topics = "${app.kafka.custom-topics.notification-topic}", groupId = "${app.kafka.group-id}-notifications-parallel", concurrency = "3", containerFactory = "kafkaListenerContainerFactory")
    public void consumeNotificationsParallel(ConsumerRecord<String, Message> record, @Header(KafkaHeaders.RECEIVED_PARTITION) int partition, Acknowledgment ack) {
        processRecord(record, partition, "Notification", ack);
    }

    @KafkaListener(topics = "${app.kafka.topic-name}", groupId = "${app.kafka.group-id}-specific", containerFactory = "kafkaListenerContainerFactory", topicPartitions = @TopicPartition(topic = "${app.kafka.topic-name}", partitions = "2"))
    public void consumeFromSpecificPartition(ConsumerRecord<String, Message> record, Acknowledgment ack) {
        processRecord(record, 2, "Specific", ack);
    }

    private void processRecord(ConsumerRecord<String, Message> record, int partition, String context, Acknowledgment ack) {
        Message message = record.value();
        try {
            log.info("[{}] {} - Received message from Partition {}: Key={}, Value={}", applicationInstance, context, partition, record.key(), message.getContent());

            logHeaderIfPresent(record, "message-type");
            processMessageByPartition(message, partition);

            ack.acknowledge();
        } catch (Exception e) {
            log.error("[{}] {} - Error processing message from Partition {}: {}", applicationInstance, context, partition, e.getMessage(), e);
            throw e; // Re-throw to trigger retry mechanism and eventual DLQ routing
        }
    }

    private void logHeaderIfPresent(ConsumerRecord<String, Message> record, String headerName) {
        getHeaderValue(record, headerName).ifPresent(value -> log.info("Header [{}]: {}", headerName, value));
    }

    private Optional<String> getHeaderValue(ConsumerRecord<String, Message> record, String headerName) {
        var header = record.headers().lastHeader(headerName);
        if (header == null) return Optional.empty();
        return Optional.of(new String(header.value(), StandardCharsets.UTF_8));
    }

    private void processMessageByPartition(Message message, int partition) {
        String content = message.getContent();
        switch (partition) {
            case 0 -> log.info("Partition 0 - High priority message: {}", content);
            case 1 -> log.info("Partition 1 - Normal priority message: {}", content);
            case 2 -> log.info("Partition 2 - Low priority message: {}", content);
            default -> log.info("Unknown partition - Default processing: {}", content);
        }
    }

    // Consume messages from Dead Letter Topic (DLT)
    @KafkaListener(topicPattern = ".*\\.DLT", groupId = "demo-group-dlt")
    public void consumeFromDLT(ConsumerRecord<String, Message> record) {
        log.error("DLT message received: {}", record.value().getContent());
    }
}