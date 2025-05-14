package na.library.kafkaessentials.consumer;

import lombok.extern.slf4j.Slf4j;
import na.library.kafkaessentials.model.Order;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.PartitionOffset;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class OrderKafkaConsumer {

    private static final BigDecimal PREMIUM_ORDER_THRESHOLD = new BigDecimal("1000");

    @KafkaListener(topics = "${app.kafka.custom-topics.order-topic}", groupId = "${app.kafka.group-id}-order", containerFactory = "kafkaListenerContainerFactory")
    public void consumeOrder(ConsumerRecord<String, Order> record, Acknowledgment ack) {
        handleMessage(record.value(), ack, this::handleStandardOrder);
    }

    @KafkaListener(groupId = "${app.kafka.group-id}-order-priority", containerFactory = "kafkaListenerContainerFactory", topicPartitions = @TopicPartition(topic = "${app.kafka.custom-topics.order-topic}", partitionOffsets = @PartitionOffset(partition = "0", initialOffset = "0")))
    public void consumePriorityOrder(Order order, Acknowledgment ack) {
        handleMessage(order, ack, this::handlePriorityOrder);
    }

    @KafkaListener(topics = "${app.kafka.custom-topics.order-topic}", groupId = "${app.kafka.group-id}-order-premium", containerFactory = "kafkaListenerContainerFactory")
    public void consumePremiumOrder(ConsumerRecord<String, Order> record, Acknowledgment ack) {
        handleMessage(record.value(), ack, this::handlePremiumOrderIfEligible);
    }

    // ======= COMMON MESSAGE HANDLER =======
    private void handleMessage(Order order, Acknowledgment ack, OrderProcessor processor) {
        try {
            processor.process(order);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Order processing failed - ID: {}, Error: {}", order.getId(), e.getMessage(), e);
            if (shouldTriggerRetry(e)) {
                throw new RuntimeException(e); // Retry mechanism will handle
            }
        }
    }

    // ======= PROCESSING STRATEGIES =======
    private void handleStandardOrder(Order order) {
        log.info("Processing order - ID: {}, Customer: {}, Total: {}, Status: {}", order.getId(), order.getCustomerId(), order.getTotalAmount(), order.getStatus());
        log.info("Items count: {}", order.getItems().size());
        // Processing logic...
    }

    private void handlePriorityOrder(Order order) {
        log.info("Processing PRIORITY order - ID: {}, Customer: {}", order.getId(), order.getCustomerId());
        // Priority logic...
    }

    private void handlePremiumOrderIfEligible(Order order) {
        if (order.getTotalAmount().compareTo(PREMIUM_ORDER_THRESHOLD) >= 0) {
            log.info("Processing PREMIUM order - ID: {}, Amount: {}", order.getId(), order.getTotalAmount());
            // Premium logic...
        } else {
            log.info("Skipped non-premium order - ID: {}, Amount: {}", order.getId(), order.getTotalAmount());
        }
    }

    private boolean shouldTriggerRetry(Exception e) {
        // Customize retry rules here
        return true; // All exceptions retried for now
    }

    // ======= Functional Interface =======
    @FunctionalInterface
    private interface OrderProcessor {
        void process(Order order);
    }
}