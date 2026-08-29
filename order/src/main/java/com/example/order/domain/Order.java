package com.example.order.domain;

import jakarta.persistence.*;

import static com.example.order.domain.Order.OrderStatus.*;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    public Order() {
        status = CREATED;
    }

    public Long getId() {
        return id;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void complete() {
        status = COMPLETED;
    }

    public void reserve() {
        if (this.status != CREATED) {
            throw new RuntimeException("생성된 단계에서만 예약할 수 있습니다.");
        }

        this.status = RESERVED;
    }

    public void cancel() {
        if (this.status != RESERVED) {
            throw new RuntimeException("에약단계에서만 취소할 수 있습니다.");
        }

        this.status = CANCELLED;
    }

    public void confirm() {
        if (this.status != RESERVED && this.status != PENDING) {
            throw new RuntimeException("예약단계 혹은 Pending 단계에서만 확정할 수 있습니다.");
        }
        this.status = CONFIRMED;
    }

    public void pending() {
        if (this.status != RESERVED) {
            throw new RuntimeException("예약단계에서만 확정할 수 있습니다.");
        }
        this.status = PENDING;
    }

    public enum OrderStatus {
        CREATED, RESERVED, CANCELLED, CONFIRMED, PENDING, COMPLETED
    }
}
