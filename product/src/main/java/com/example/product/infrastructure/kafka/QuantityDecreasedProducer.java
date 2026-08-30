package com.example.product.infrastructure.kafka;

import com.example.product.infrastructure.kafka.dto.QuantityDecreasedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class QuantityDecreasedProducer {
    private final KafkaTemplate<String, QuantityDecreasedEvent> kafkaTemplate;

    public QuantityDecreasedProducer(KafkaTemplate<String, QuantityDecreasedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(QuantityDecreasedEvent event) {
        kafkaTemplate.send(
                "quantity-decreased",
                event.orderId().toString(),
                event
        );
    }
}
