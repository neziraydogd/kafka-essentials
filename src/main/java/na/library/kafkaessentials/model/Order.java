package na.library.kafkaessentials.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    
    private String id;
    private String customerId;
    private List<OrderItem> items;
    private BigDecimal totalAmount;
    private LocalDateTime orderDate;
    private OrderStatus status;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {
        private String productId;
        private String productName;
        private int quantity;
        private BigDecimal unitPrice;
    }
    
    public enum OrderStatus {
        CREATED, PROCESSING, SHIPPED, DELIVERED, CANCELLED
    }
    
    // Fabrika metodu - toplam tutarı hesaplayarak yeni sipariş oluşturur
    public static Order createOrder(String customerId, List<OrderItem> items) {
        BigDecimal total = items.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
        return Order.builder()
                .id(UUID.randomUUID().toString())
                .customerId(customerId)
                .items(items)
                .totalAmount(total)
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.CREATED)
                .build();
    }
}