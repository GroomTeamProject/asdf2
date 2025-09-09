package io.goorm.team02.core.orders.dto;

import io.goorm.team02.core.orders.domain.Order;
import io.goorm.team02.core.orders.domain.OrderItem;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class OrderRequest {
    private String deliveryAddress;
    private String detailAddress;
    private String phone;
    private String orderMemo;
    private List<OrderItemRequest> items;

    // Order 엔티티로 변환
    public Order toOrder() {
        Order order = new Order();
        order.setDeliveryAddress(deliveryAddress);
        order.setDetailAddress(detailAddress);
        order.setPhone(phone);
        order.setOrderMemo(orderMemo);

        if (items != null) {
            List<OrderItem> orderItems = items.stream()
                    .map(OrderItemRequest::toOrderItem)
                    .collect(Collectors.toList());
            order.setItems(orderItems);
            orderItems.forEach(i -> i.setOrder(order)); // 양방향 연관 설정
        }

        return order;
    }
}
