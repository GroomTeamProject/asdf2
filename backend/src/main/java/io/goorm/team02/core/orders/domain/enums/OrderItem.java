package io.goorm.team02.core.orders.domain.enums;

import io.goorm.team02.core.orders.domain.Order;
import jakarta.persistence.*;

@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long menuId;
    private String menuName;
    private int quantity;
    private int totalPrice;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    // Getter, Setter
}
