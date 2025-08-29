package io.goorm.team02.core.payments.domain;

import io.goorm.team02.core.common.domain.BaseEntity;
import io.goorm.team02.core.orders.domain.Order;
import io.goorm.team02.core.payments.domain.enmus.PaymentMethod;
import io.goorm.team02.core.payments.domain.enmus.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne
	@JoinColumn(name = "order_id", nullable = false)
	private Order order;

	@Column(name = "payment_key", unique = true, length = 200)
	private String paymentKey;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PaymentMethod paymentMethod;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal amount;

	@Enumerated(EnumType.STRING)
	private PaymentStatus status = PaymentStatus.PENDING;

	@Column(name = "pg_provider", length = 50)
	private String pgProvider;

	@Column(name = "pg_tid", length = 100)
	private String pgTid;

	private LocalDateTime approvedAt;

	@Column(columnDefinition = "TEXT")
	private String failedReason;

}
