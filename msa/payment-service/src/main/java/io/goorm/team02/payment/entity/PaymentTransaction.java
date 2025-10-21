package io.goorm.team02.payment.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transactions")
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    private String type;            // APPROVAL / CANCEL / FAIL
    private String status;          // PENDING / COMPLETED / FAILED
    private BigDecimal amount;
    private String pgTransactionId; // PG사 트랜잭션 ID
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // getter / setter
}
