// domain/OrderItemOption.java
package io.goorm.team02.core.orders.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "order_item_options")
public class OrderItemOption {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;
	private int price;

	@ManyToOne
	@JoinColumn(name = "order_item_id")
	private OrderItem orderItem;

	// 생성자
	public OrderItemOption() {
	}

	// Getter / Setter
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public OrderItem getOrderItem() {
		return orderItem;
	}

	public void setOrderItem(OrderItem orderItem) {
		this.orderItem = orderItem;
	}
}
