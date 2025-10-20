package io.goorm.team02.order.entity;

import io.goorm.team02.dto.orders.OrderRequest;
import io.goorm.team02.dto.orders.OrderResponse;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "order_item_options")
@Data
public class OrderItemOption {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "order_item_id", nullable = false)
	@JsonIgnore
	private OrderItem orderItem;

	@Column(nullable = false, length = 50)
	private String optionName;

	@Column(nullable = false, length = 50)
	private String optionItemName;

	@Column(nullable = false)
	private int additionalPrice = 0;

	/**
	 * OrderRequest에서 OrderItemOption 생성 (도메인 로직)
	 */
	public static OrderItemOption create(OrderItem orderItem,
			OrderRequest.OrderItemOptionRequest optionRequest) {
		OrderItemOption option = new OrderItemOption();
		option.setOrderItem(orderItem);
		option.setOptionName(optionRequest.optionName());
		option.setOptionItemName(optionRequest.optionItemName());
		option.setAdditionalPrice(optionRequest.additionalPrice());
		return option;
	}

	/**
	 * OrderItemOptionResponse로 변환
	 */
	public OrderResponse.OrderItemOptionResponse toResponse() {
		return new OrderResponse.OrderItemOptionResponse(
				this.id,
				this.optionName,
				this.optionItemName,
				this.additionalPrice);
	}

}
