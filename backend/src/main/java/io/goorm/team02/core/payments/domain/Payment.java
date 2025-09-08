package io.goorm.team02.core.payments.domain;

import io.goorm.team02.core.payments.domain.enums.PaymentMethod;
import io.goorm.team02.core.payments.domain.enums.PaymentStatus;
import io.goorm.team02.core.orders.domain.Order;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
public class Payment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne
	@JoinColumn(name = "order_id", nullable = false)
	private Order order;

	@Column(name = "payment_key", unique = true)
	private String paymentKey;

	@Enumerated(EnumType.STRING)
	@Column(name = "payment_method", nullable = false)
	private PaymentMethod method;

	@Column(nullable = false)
	private BigDecimal amount;

	@Enumerated(EnumType.STRING)
	private PaymentStatus status = PaymentStatus.PENDING;

	@Column(name = "pg_provider")
	private String pgProvider;

	@Column(name = "pg_tid")
	private String pgTid;

	@Column(name = "approved_at")
	private LocalDateTime approvedAt;

	@Column(name = "failed_reason")
	private String failedReason;

	@Column(name = "created_at")
	private LocalDateTime createdAt = LocalDateTime.now();

	@Column(name = "updated_at")
	private LocalDateTime updatedAt = LocalDateTime.now();
}
