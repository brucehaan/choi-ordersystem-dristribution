package com.example.point.domain;

import jakarta.persistence.*;

@Table(name = "point_transaction_histories")
@Entity
public class PointTransactionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String requestId;

    private Long pointId;

    private Long amount;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    public PointTransactionHistory() {
    }

    public PointTransactionHistory(String requestId, Long pointId, Long amount, TransactionType transactionType) {
        this.requestId = requestId;
        this.pointId = pointId;
        this.amount = amount;
        this.transactionType = transactionType;
    }

    public enum TransactionType {
        USE, CANCEL
    }
}
