package na.library.kafkaessentials.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import na.library.kafkaessentials.model.Message;
import na.library.kafkaessentials.model.Order;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topic-name}")
    private String topicName;

    @Value("${app.kafka.custom-topics.notification-topic}")
    private String notificationTopic;

    @Value("${app.kafka.custom-topics.order-topic}")
    private String orderTopic;

    /**
     * Sends a basic message to the main topic.
     */
    public void sendMessage(Message message) {
        String key = message.getSender();

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topicName, key, message); //async

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Message sent successfully - Topic: {}, Partition: {}, Offset: {}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Message sending failed - {}", ex.getMessage(), ex);
            }
        });
    }

    /**
     * Sends a message enriched with custom headers.
     */
    public void sendMessageWithHeaders(Message message) {
        String key = message.getSender();

        List<Header> headers = new ArrayList<>();
        headers.add(new RecordHeader("message-type", message.getType().toString().getBytes(StandardCharsets.UTF_8)));
        headers.add(new RecordHeader("correlation-id", UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8)));
        headers.add(new RecordHeader("timestamp", message.getTimestamp().toString().getBytes(StandardCharsets.UTF_8)));

        ProducerRecord<String, Object> record = new ProducerRecord<>(
                topicName,
                null,
                System.currentTimeMillis(),
                key,
                message,
                headers
        );

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(record);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                logSendResult(result, "Header-enriched message", null);
            } else {
                logSendFailure(ex, "Header-enriched message", null);
            }
        });

    }

    /**
     * Sends a message to the notification topic.
     */
    public void sendNotification(Message message) {
        if (message.getType() != Message.MessageType.NOTIFICATION) {
            message = Message.builder()
                    .id(message.getId())
                    .content(message.getContent())
                    .sender(message.getSender())
                    .timestamp(message.getTimestamp())
                    .type(Message.MessageType.NOTIFICATION)
                    .build();
        }

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(notificationTopic, message.getSender(), message);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                logSendResult(result, "Notification", null);
            } else {
                logSendFailure(ex, "Notification", null);
            }
        });
    }

    /**
     * Sends an order message to the order topic.
     */
    public void sendOrder(Order order) {
        String key = order.getId();

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(orderTopic, key, order);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                logSendResult(result, "Order", "Order ID: " + order.getId());
            } else {
                logSendFailure(ex, "Order", "Order ID: " + order.getId());
            }
        });

    }

    /**
     * Sends a message to a specific Kafka partition.
     */
    public void sendToSpecificPartition(
            String topic, int partition, String key, Object message) {

        ProducerRecord<String, Object> record = new ProducerRecord<>(
                topic,
                partition,
                key,
                message
        );

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(record);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                logSendResult(result, "Message (Specific Partition)", null);
            } else {
                logSendFailure(ex, "Message (Specific Partition)", "Partition: " + partition);
            }
        });

    }

    /**
     * Sends a batch of messages to the main topic.
     */
    public void sendBatchMessages(List<Message> messages) {
        for (Message message : messages) {
            kafkaTemplate.send(topicName, message.getSender(), message);
        }
        kafkaTemplate.flush();
        log.info("Batch of {} messages sent", messages.size());
    }

    private void logSendResult(SendResult<String, Object> result, String type, String extra) {
        log.info("{} sent | Topic: {}, Partition: {}, Offset: {}{}",
                type,
                result.getRecordMetadata().topic(),
                result.getRecordMetadata().partition(),
                result.getRecordMetadata().offset(),
                extra != null ? " | " + extra : ""
        );
    }

    private void logSendFailure(Throwable ex, String type, String extra) {
        log.error("{} failed{} | Reason: {}", type, extra != null ? " - " + extra : "", ex.getMessage(), ex);
    }
}