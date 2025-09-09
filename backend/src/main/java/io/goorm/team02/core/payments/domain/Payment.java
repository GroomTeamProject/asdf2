package io.goorm.team02.core.payments.domain;

import io.goorm.team02.core.orders.domain.Order;
import io.goorm.team02.core.payments.domain.enums.PaymentMethod;
import io.goorm.team02.core.payments.domain.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Payment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = false)
	private Order order;

	@Column(unique = true)
	private String paymentKey;

	@Enumerated(EnumType.STRING)
	private PaymentMethod method;

	private BigDecimal amount;

	@Enumerated(EnumType.STRING)
	private PaymentStatus status = PaymentStatus.PENDING;

	private String pgProvider;

	private String pgTid;

	private LocalDateTime approvedAt;

	private String failedReason;

	private LocalDateTime createdAt = LocalDateTime.now();

	private LocalDateTime updatedAt = LocalDateTime.now();
}
