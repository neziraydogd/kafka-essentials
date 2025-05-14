package na.library.kafkaessentials.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    
    private String id;
    private String content;
    private String sender;
    private LocalDateTime timestamp;
    private MessageType type;
    
    public enum MessageType {
        INFO, WARNING, ERROR, NOTIFICATION
    }
    
    // Fabrika metodu - yeni mesaj oluşturmak için pratik yöntem
    public static Message createMessage(String content, String sender, MessageType type) {
        return Message.builder()
                .id(UUID.randomUUID().toString())
                .content(content)
                .sender(sender)
                .timestamp(LocalDateTime.now())
                .type(type)
                .build();
    }
}