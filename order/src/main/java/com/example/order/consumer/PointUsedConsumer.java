package com.example.order.consumer;

import com.example.order.application.OrderService;
import com.example.order.consumer.dto.PointUsedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PointUsedConsumer {
    private final OrderService orderService;

    public PointUsedConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    @KafkaListener(
            topics = "point-used",
            groupId = "point-used-consumer",
            properties = {
                    "spring.json.value.default.type=com.example.order.consumer.dto.PointUsedEvent"
            }
    )
    public void handle(PointUsedEvent event) {
        orderService.complete(event.orderId());
    }
}
