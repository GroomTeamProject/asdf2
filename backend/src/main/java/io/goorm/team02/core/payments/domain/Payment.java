package io.goorm.team02.core.payments.domain;

import io.goorm.team02.core.orders.domain.Order;
import io.goorm.team02.core.payments.domain.enums.PaymentMethod;
import io.goorm.team02.core.payments.domain.enums.PaymentStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// orders 테이블 참조
	@ManyToOne
	@JoinColumn(name = "order_id")
	private Order order;

	@Enumerated(EnumType.STRING)
	@Column(name = "status") 
	private PaymentStatus status;

	@Enumerated(EnumType.STRING)
	@Column(name = "payment_method")
	private PaymentMethod method;

	@Column(name = "amount")
	private BigDecimal amount;

	@Column(name = "payment_key")
	private String paymentKey;

	@Column(name = "pg_provider")
	private String pgProvider;

	@Column(name = "pg_tid")
	private String pgTid;

	@Column(name = "created_at")
	private LocalDateTime createdAt = LocalDateTime.now();

	@Column(name = "updated_at")
	private LocalDateTime updatedAt = LocalDateTime.now();

	public Long getId() {
		return id;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public PaymentStatus getStatus() {
		return status;
	}

	public void setStatus(PaymentStatus status) {
		this.status = status;
	}

	public PaymentMethod getMethod() {
		return method;
	}

	public void setMethod(PaymentMethod method) {
		this.method = method;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getPaymentKey() {
		return paymentKey;
	}

	public void setPaymentKey(String paymentKey) {
		this.paymentKey = paymentKey;
	}

	public String getPgProvider() {
		return pgProvider;
	}

	public void setPgProvider(String pgProvider) {
		this.pgProvider = pgProvider;
	}

	public String getPgTid() {
		return pgTid;
	}

	public void setPgTid(String pgTid) {
		this.pgTid = pgTid;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
}
