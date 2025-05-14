package na.library.kafkaessentials.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import na.library.kafkaessentials.model.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaSyncProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RetryTemplate retryTemplate;

    @Value("${app.kafka.topic-name}")
    private String topicName;

    public boolean sendMessageSync(String key, Message message) {
        try {
            retryTemplate.execute(context -> {
                kafkaTemplate.send(topicName, key, message).get(5, TimeUnit.SECONDS);
                return null;
            });
            return true;
        } catch (TimeoutException ex) {
            log.error("Kafka message could not be sent due to timeout -> {}", ex.getMessage(), ex);
            return false;
        } catch (ExecutionException ex) {
            log.error("Kafka message could not be sent due to execution error -> {}", ex.getMessage(), ex);
            return false;
        } catch (Exception ex) {
            log.error("Kafka message could not be sent due to unexpected error -> {}", ex.getMessage(), ex);
            return false;
        }
    }
}