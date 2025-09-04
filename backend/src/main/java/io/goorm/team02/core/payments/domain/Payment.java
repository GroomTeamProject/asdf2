package io.goorm.team02.core.payments.domain;

import io.goorm.team02.core.orders.domain.Order;
import io.goorm.team02.core.payments.domain.enums.PaymentMethod;
import io.goorm.team02.core.payments.domain.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
public class Payment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String paymentKey;

	@Enumerated(EnumType.STRING)
	private PaymentStatus status;

	@Enumerated(EnumType.STRING)
	private PaymentMethod method; // 필드 이름 정확히 확인! -> setPaymentMethod 호출 가능

	private BigDecimal amount;

	private String pgProvider;
	private String pgTid;

	private String failedReason; // setFailedReason 호출 가능

	// Order와 1:1 연관관계
	@OneToOne
	@JoinColumn(name = "order_id")
	private Order order;

	// 편의 메서드: Service에서 setOrderId(Long) 호출 가능
	public void setOrderId(Long orderId) {
		if (this.order == null) {
			this.order = new Order();
		}
		this.order.setId(orderId);
	}
}
