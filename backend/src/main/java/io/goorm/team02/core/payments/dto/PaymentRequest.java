package io.goorm.team02.core.payments.dto;

import java.math.BigDecimal;
import java.util.List;

public class PaymentRequest {
    private String paymentKey;
    private String pgProvider;
    private String pgTid;
    private BigDecimal amount;
    private Long orderId;
    private List<Item> items;

    // Getter / Setter
    public String getPaymentKey() {
        return paymentKey;
    }

    public void setPaymentKey(String paymentKey) {
        this.paymentKey = paymentKey;
    }

    public String getPgProvider() {
        return pgProvider;
    }

    public void setPgProvider(String pgProvider) {
        this.pgProvider = pgProvider;
    }

    public String getPgTid() {
        return pgTid;
    }

    public void setPgTid(String pgTid) {
        this.pgTid = pgTid;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderIdString(Long orderId) {
        this.orderId = orderId;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public static class Item {
        private Long productId;
        private String name;
        private BigDecimal price;
        private int quantity;
        private Long menuId;

        // Getter / Setter
        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public Long getMenuId() {
            return menuId;
        }

        public void setMenuId(Long menuId) {
            this.menuId = menuId;
        }
    }
}
