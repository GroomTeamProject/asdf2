package io.goorm.team02.core.orders.domain;

import io.goorm.team02.core.menus.domain.Menu;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "order_items")
@Data
public class OrderItem{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "order_id", nullable = false)
	private Order order;

	@ManyToOne
	@JoinColumn(name = "menu_id", nullable = false)
	private Menu menu;

	@Column(name = "menu_name", nullable = false, length = 100)
	private String menuName; // 주문 당시 스냅샷

	@Column(name = "menu_price", nullable = false, precision = 10, scale = 2)
	private BigDecimal menuPrice;

	@Column(nullable = false)
	private Integer quantity;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal totalPrice;

	@OneToMany(mappedBy = "orderItem", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<OrderItemOption> options;

	// 도메인 비즈니스 로직
	/**
	 * 주문 아이템 총액 계산 (메뉴 가격 × 수량 + 옵션 추가 가격)
	 */
	public void calculateTotalPrice() {
		BigDecimal basePrice = this.menuPrice.multiply(BigDecimal.valueOf(this.quantity));
		BigDecimal optionPrice = calculateOptionPrice();
		this.totalPrice = basePrice.add(optionPrice);
	}
	
	/**
	 * 옵션 추가 가격 계산 (수량 반영)
	 */
	private BigDecimal calculateOptionPrice() {
		if (options == null || options.isEmpty()) {
			return BigDecimal.ZERO;
		}
		
		// 옵션 가격의 합계에 수량을 곱함
		BigDecimal totalOptionPrice = options.stream()
			.map(OrderItemOption::getAdditionalPrice)
			.reduce(BigDecimal.ZERO, BigDecimal::add);
		
		return totalOptionPrice.multiply(BigDecimal.valueOf(this.quantity));
	}
	
	/**
	 * 주문 아이템 생성 팩토리 메서드
	 */
	public static OrderItem create(Order order, Menu menu, Integer quantity, List<OrderItemOption> options) {
		OrderItem orderItem = new OrderItem();
		orderItem.setOrder(order);
		orderItem.setMenu(menu);
		orderItem.setMenuName(menu.getName());
		orderItem.setMenuPrice(menu.getPrice());
		orderItem.setQuantity(quantity);
		orderItem.setOptions(options != null ? options : new ArrayList<>());
		orderItem.calculateTotalPrice();
		return orderItem;
	}

}
