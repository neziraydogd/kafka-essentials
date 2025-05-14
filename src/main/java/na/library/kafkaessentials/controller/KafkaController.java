package na.library.kafkaessentials.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import na.library.kafkaessentials.model.Message;
import na.library.kafkaessentials.model.Order;
import na.library.kafkaessentials.service.KafkaProducerService;
import na.library.kafkaessentials.service.KafkaSyncProducerService;
import na.library.kafkaessentials.service.KafkaTopicService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/kafka")
@RequiredArgsConstructor
@Slf4j
public class KafkaController {

    private final KafkaProducerService producerService;
    private final KafkaSyncProducerService kafkaSyncProducerService;
    private final KafkaTopicService topicService;


    /**
     * Send a simple message to Kafka.
     */
    @PostMapping("/messages")
    public ResponseEntity<String> sendMessage(@RequestBody Message message) {
        log.info("Sending message via REST API: {}", message);
        producerService.sendMessage(message);
        return ResponseEntity.ok("Message sent: " + message.getId());
    }

    /**
     * Send a message with custom headers to Kafka.
     */
    @PostMapping("/messages/with-headers")
    public ResponseEntity<String> sendMessageWithHeaders(@RequestBody Message message) {
        log.info("Sending message with headers via REST API: {}", message);
        producerService.sendMessageWithHeaders(message);
        return ResponseEntity.ok("Message with headers sent: " + message.getId());
    }

    /**
     * Send a notification message to Kafka.
     */
    @PostMapping("/notifications")
    public ResponseEntity<String> sendNotification(@RequestBody Message message) {
        log.info("Sending notification via REST API: {}", message);
        producerService.sendNotification(message);
        return ResponseEntity.ok("Notification sent: " + message.getId());
    }

    /**
     * Send an order object to Kafka.
     */
    @PostMapping("/orders")
    public ResponseEntity<String> sendOrder(@RequestBody Order order) {
        log.info("Sending order via REST API: {}", order);
        producerService.sendOrder(order);
        return ResponseEntity.ok("Order sent: " + order.getId());
    }

    /**
     * Send a message to a specific partition of a topic.
     */
    @PostMapping("/messages/partition/{partition}")
    public ResponseEntity<String> sendToPartition(
            @PathVariable int partition,
            @RequestParam String topic,
            @RequestParam String key,
            @RequestBody Message message) {
        log.info("Sending message to specific partition: Topic={}, Partition={}, Key={}, Message={}",
                topic, partition, key, message);
        producerService.sendToSpecificPartition(topic, partition, key, message);
        return ResponseEntity.ok("Message sent to Topic: " + topic + ", Partition: " + partition);
    }

    /**
     * List all available Kafka topics.
     */
    @GetMapping("/topics")
    public ResponseEntity<Set<String>> listTopics() {
        log.info("Fetching list of all Kafka topics.");
        Set<String> topics = topicService.listAllTopics();
        return ResponseEntity.ok(topics);
    }

    /**
     * Get details about a specific Kafka topic.
     */
    @GetMapping("/topics/{topic}")
    public ResponseEntity<Map<String, Object>> getTopicDetails(@PathVariable String topic) {
        log.info("Fetching details for topic: {}", topic);
        Map<String, Object> details = topicService.getTopicDetails(topic);
        return ResponseEntity.ok(details);
    }

    /**
     * Create a new Kafka topic.
     */
    @PostMapping("/topics")
    public ResponseEntity<String> createTopic(
            @RequestParam String name,
            @RequestParam(defaultValue = "1") int partitions,
            @RequestParam(defaultValue = "1") short replicationFactor) {
        log.info("Creating new topic: {}, partitions: {}, replication: {}", name, partitions, replicationFactor);
        topicService.createTopic(name, partitions, replicationFactor);
        return ResponseEntity.ok("Topic created: " + name);
    }

    /**
     * Delete a Kafka topic.
     */
    @DeleteMapping("/topics/{topic}")
    public ResponseEntity<String> deleteTopic(@PathVariable String topic) {
        log.info("Deleting topic: {}", topic);
        topicService.deleteTopic(topic);
        return ResponseEntity.ok("Topic deleted: " + topic);
    }

    /**
     * Add partitions to an existing Kafka topic.
     */
    @PostMapping("/topics/{topic}/partitions")
    public ResponseEntity<String> addPartitions(
            @PathVariable String topic,
            @RequestParam int newPartitionCount) {
        log.info("Increasing partitions for topic: {}, new count: {}", topic, newPartitionCount);
        topicService.increasePartitions(topic, newPartitionCount);
        return ResponseEntity.ok("Partitions updated. New count: " + newPartitionCount);
    }

    /**
     * Send a batch of messages to Kafka.
     */
    @PostMapping("/messages/batch")
    public ResponseEntity<String> sendBatchMessages(@RequestBody List<Message> messages) {
        log.info("Sending batch messages. Total: {}", messages.size());
        producerService.sendBatchMessages(messages);
        return ResponseEntity.ok("Batch messages sent. Total: " + messages.size());
    }

    /**
     * Update configuration settings of a Kafka topic.
     */
    @PutMapping("/topics/{topic}/config")
    public ResponseEntity<String> updateTopicConfig(
            @PathVariable String topic,
            @RequestBody Map<String, String> configs) {
        log.info("Updating topic configuration: {}, configs: {}", topic, configs);
        topicService.updateTopicConfig(topic, configs);
        return ResponseEntity.ok("Topic configuration updated: " + topic);
    }

    @PostMapping("sendMessageSync")
    public ResponseEntity<String> sendMessageSync(@RequestBody Message message) {
        boolean success = kafkaSyncProducerService.sendMessageSync(message.getSender(), message);
        if (success) {
            return ResponseEntity.ok("Message sent successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Message sent failed, please try again.");
        }
    }

    /**
     * Health check endpoint for Kafka service.
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        log.info("Performing Kafka service health check");
        return ResponseEntity.ok("Kafka service is active");
    }
}