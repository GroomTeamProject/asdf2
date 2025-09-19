package io.goorm.team02.core.orders.dto;

import java.math.BigDecimal;

public class OrderItemResponse {

    private Long menuId;
    private String menuName;
    private int quantity;
    private BigDecimal price;

    public OrderItemResponse(Long menuId, String menuName, int quantity, BigDecimal price) {
        this.menuId = menuId;
        this.menuName = menuName;
        this.quantity = quantity;
        this.price = price;
    }
            
            
    public Long getMenuId() {
        return menuId;
    }

    public String getMenuName() {
        return menuName;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }
}
