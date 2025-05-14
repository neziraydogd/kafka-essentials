package na.library.kafkaessentials.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaTopicService {

    private final KafkaAdmin kafkaAdmin;

    /**
     * Creates a new topic with the given configuration.
     */
    public void createTopic(String topicName, int numPartitions, short replicationFactor) {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            Map<String, String> topicConfigs = new HashMap<>();
            topicConfigs.put(TopicConfig.RETENTION_MS_CONFIG, "604800000"); // 7 days retention
            topicConfigs.put(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_DELETE);

            NewTopic newTopic = new NewTopic(topicName, numPartitions, replicationFactor)
                    .configs(topicConfigs);

            adminClient.createTopics(Collections.singleton(newTopic)).all().get();
            log.info("Topic '{}' created successfully. Partitions: {}, Replication factor: {}",
                    topicName, numPartitions, replicationFactor);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof TopicExistsException) {
                log.warn("Topic '{}' already exists.", topicName);
            } else {
                log.error("Error creating topic '{}': {}", topicName, e.getMessage(), e);
                throw new RuntimeException("Failed to create topic", e);
            }
        } catch (Exception e) {
            log.error("Error creating topic '{}': {}", topicName, e.getMessage(), e);
            throw new RuntimeException("Failed to create topic", e);
        }
    }

    /**
     * Deletes the specified topic.
     */
    public void deleteTopic(String topicName) {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            adminClient.deleteTopics(Collections.singleton(topicName)).all().get();
            log.info("Topic '{}' deleted successfully.", topicName);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof UnknownTopicOrPartitionException) {
                log.warn("Topic '{}' not found for deletion.", topicName);
            } else {
                log.error("Error deleting topic '{}': {}", topicName, e.getMessage(), e);
                throw new RuntimeException("Failed to delete topic", e);
            }
        } catch (Exception e) {
            log.error("Error deleting topic '{}': {}", topicName, e.getMessage(), e);
            throw new RuntimeException("Failed to delete topic", e);
        }
    }

    /**
     * Updates the configuration of an existing topic.
     */
    public void updateTopicConfig(String topicName, Map<String, String> updatedConfigs) {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            ConfigResource configResource = new ConfigResource(ConfigResource.Type.TOPIC, topicName);

            List<AlterConfigOp> configOps = new ArrayList<>();
            updatedConfigs.forEach((key, value) ->
                    configOps.add(new AlterConfigOp(new ConfigEntry(key, value), AlterConfigOp.OpType.SET))
            );

            adminClient.incrementalAlterConfigs(
                    Collections.singletonMap(configResource, configOps)
            ).all().get();

            log.info("Configuration of topic '{}' updated successfully: {}", topicName, updatedConfigs);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof UnknownTopicOrPartitionException) {
                log.error("Topic '{}' not found for configuration update.", topicName);
            } else {
                log.error("Error updating topic config '{}': {}", topicName, e.getMessage(), e);
            }
            throw new RuntimeException("Failed to update topic config", e);
        } catch (Exception e) {
            log.error("Error updating topic config '{}': {}", topicName, e.getMessage(), e);
            throw new RuntimeException("Failed to update topic config", e);
        }
    }

    /**
     * Retrieves detailed information about a topic.
     */
    public Map<String, Object> getTopicDetails(String topicName) {
        Map<String, Object> topicDetails = new HashMap<>();

        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            TopicDescription description = adminClient
                    .describeTopics(Collections.singleton(topicName))
                    .topicNameValues()
                    .get(topicName)
                    .get();

            Config config = adminClient
                    .describeConfigs(Collections.singleton(new ConfigResource(ConfigResource.Type.TOPIC, topicName)))
                    .values()
                    .get(new ConfigResource(ConfigResource.Type.TOPIC, topicName))
                    .get();

            topicDetails.put("name", description.name());
            topicDetails.put("internal", description.isInternal());

            List<Map<String, Object>> partitions = new ArrayList<>();
            for (TopicPartitionInfo partition : description.partitions()) {
                Map<String, Object> partitionData = new HashMap<>();
                partitionData.put("partition", partition.partition());
                partitionData.put("leader", partition.leader().id());

                partitionData.put("replicas", partition.replicas().stream().map(Node::id).toList());
                partitionData.put("isr", partition.isr().stream().map(Node::id).toList());

                partitions.add(partitionData);
            }

            topicDetails.put("partitions", partitions);

            Map<String, String> configMap = new HashMap<>();
            config.entries().forEach(entry -> configMap.put(entry.name(), entry.value()));
            topicDetails.put("config", configMap);

            return topicDetails;

        } catch (ExecutionException e) {
            if (e.getCause() instanceof UnknownTopicOrPartitionException) {
                log.error("Topic '{}' not found.", topicName);
            } else {
                log.error("Error retrieving topic details '{}': {}", topicName, e.getMessage(), e);
            }
            throw new RuntimeException("Failed to retrieve topic details", e);
        } catch (Exception e) {
            log.error("Error retrieving topic details '{}': {}", topicName, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve topic details", e);
        }
    }

    /**
     * Increases the number of partitions for a topic (partition count cannot be decreased).
     */
    public void increasePartitions(String topicName, int newPartitionCount) {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            adminClient.createPartitions(Collections.singletonMap(
                    topicName, NewPartitions.increaseTo(newPartitionCount)
            )).all().get();

            log.info("Partition count for topic '{}' updated to {}", topicName, newPartitionCount);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof UnknownTopicOrPartitionException) {
                log.error("Topic '{}' not found for partition increase.", topicName);
            } else {
                log.error("Error increasing partitions for topic '{}': {}", topicName, e.getMessage(), e);
            }
            throw new RuntimeException("Failed to increase partitions", e);
        } catch (Exception e) {
            log.error("Error increasing partitions for topic '{}': {}", topicName, e.getMessage(), e);
            throw new RuntimeException("Failed to increase partitions", e);
        }
    }

    /**
     * Lists all available topics in the Kafka cluster.
     */
    public Set<String> listAllTopics() {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            return adminClient.listTopics().names().get();
        } catch (Exception e) {
            log.error("Error listing topics: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to list topics", e);
        }
    }
}