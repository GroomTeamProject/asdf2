package io.goorm.team02.core.orders.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

import io.goorm.team02.core.orders.domain.enums.OrderStatus;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerName;
    private String phone;
    private String deliveryAddress;
    private String deliveryDetailAddress;
    private String orderMemo;

    private int totalAmount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.COOKING;

    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "order")
    private List<OrderItem> items;

    public void setStatus(OrderStatus cooking) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setStatus'");
    }

    public Iterable<Order> getItems() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getItems'");
    }

    public Object setOrder(Order order) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setOrder'");
    }

    public OrderStatus getStatus() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getStatus'");
    }

    // Getter, Setter
}
