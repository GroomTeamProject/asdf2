package io.goorm.team02.core.orders.dto;

import io.goorm.team02.core.orders.domain.OrderItem;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemRequest {
    private String name;
    private int quantity;

    public OrderItem toOrderItem() {
        OrderItem item = new OrderItem();
        item.setName(name);
        item.setQuantity(quantity);
        return item;
    }
}
