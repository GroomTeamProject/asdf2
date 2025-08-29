package io.goorm.team02.core.order.domain.dto;

import io.goorm.team02.core.order.domain.Order;

public record OrderRequest(
		String orderMessage
) {

	public Order toEntity() {
		Order order = new Order();
		order.setOrderMessage(this.orderMessage);

		return order;
	}

}
