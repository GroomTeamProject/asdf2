package io.goorm.team02.core.orders.controller.dto;

import io.goorm.team02.core.orders.domain.Order;


public record OrderRequest(
		String orderMemo
) {

	public Order toEntity() {
		Order order = new Order();
		order.setOrderMemo(this.orderMemo);

		return order;
	}

}
