package io.goorm.team02.core.orders.domain;

import io.goorm.team02.core.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "order_item_options")
@Data
public class OrderItemOption{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "order_item_id", nullable = false)
	private OrderItem orderItem;

	@Column(nullable = false, length = 50)
	private String optionName;

	@Column(nullable = false, length = 50)
	private String optionItemName;

	@Column(precision = 10, scale = 2)
	private BigDecimal additionalPrice = BigDecimal.ZERO;

}
