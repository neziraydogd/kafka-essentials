package na.library.kafkaessentials.consumer;

import lombok.extern.slf4j.Slf4j;
import na.library.kafkaessentials.model.Message;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SimpleKafkaConsumer {

    /**
     * Kafka consumer that listens to the main topic.
     */
    @KafkaListener(topics = "${app.kafka.topic-name}", groupId = "${app.kafka.group-id}", containerFactory = "kafkaListenerContainerFactory")
    public void consumeMessage(@Payload Message message, @Header(KafkaHeaders.RECEIVED_PARTITION) int partition, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic, @Header(KafkaHeaders.RECEIVED_KEY) String key, @Header(KafkaHeaders.OFFSET) long offset, Acknowledgment ack) {

        logIncomingMessage(topic, partition, offset, key, message);
        handleMainTopicMessage(message, ack);
    }

    /**
     * Kafka consumer that listens to notification messages.
     */
    @KafkaListener(topics = "${app.kafka.custom-topics.notification-topic}", groupId = "${app.kafka.group-id}-notification", containerFactory = "kafkaListenerContainerFactory")
    public void consumeNotification(ConsumerRecord<String, Message> record, Acknowledgment ack) {
        Message message = record.value();
        log.info("Notification received - Sender: {}, Content: {}", message.getSender(), message.getContent());
        processWithAcknowledgment(ack, "An error occurred while processing the notification");
    }

    /**
     * Resilient Kafka consumer example with retry mechanism.
     * In the retryableKafkaListenerContainerFactory, since no explicit AckMode was configured,
     * the default mode AckMode.BATCH is applied. In this mode, acknowledgment is handled automatically
     * by the container after each batch of messages is processed.
     * As a result, the Acknowledgment parameter is not injected into the listener method,
     * and attempting to use it would lead to an IllegalStateException.
     * Therefore, acknowledgment was not used in this configuration.
     */
    @KafkaListener(topics = "${app.kafka.topic-name}", groupId = "${app.kafka.group-id}-retry", containerFactory = "retryableKafkaListenerContainerFactory")
    public void consumeWithRetry(Message message) {
        try {
            log.info("Retry consumer - Message received: {}", message.getContent());
            simulateFailureIfRequired(message);
        } catch (Exception e) {
            log.error("Error occurred while processing the message. It will be retried by RetryableListener: {}", e.getMessage());
            throw e; // Let retry mechanism handle it
        }
    }

    // ========== PRIVATE METHODS ==========

    private void logIncomingMessage(String topic, int partition, long offset, String key, Message message) {
        log.info("Message received - Topic: {}, Partition: {}, Offset: {}, Key: {}, ID: {}, Content: {}", topic, partition, offset, key, message.getId(), message.getContent());
    }

    private void handleMainTopicMessage(Message message, Acknowledgment ack) {
        try {
            if (message.getType() == Message.MessageType.ERROR) {
                log.warn("ERROR message detected: {}", message.getContent());
            }
            ack.acknowledge();
        } catch (Exception e) {
            log.error("An error occurred while processing the message: {}", e.getMessage(), e);
            ack.acknowledge(); // Optional: you may skip ack to reprocess later or handle via DLQ
        }
    }

    private void processWithAcknowledgment(Acknowledgment ack, String errorMessage) {
        try {
            // Process notification...
            ack.acknowledge();
        } catch (Exception e) {
            log.error("{}: {}", errorMessage, e.getMessage(), e);
            ack.acknowledge(); // Optional: consider reprocess or DLQ strategy
        }
    }

    private void simulateFailureIfRequired(Message message) {
        if (message.getContent().contains("FAIL")) {
            throw new RuntimeException("Simulated failure condition");
        }
    }
}