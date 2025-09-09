// src/main/java/io/goorm/team02/core/payments/dto/PaymentRequest.java
package io.goorm.team02.core.payments.dto;

import java.math.BigDecimal;

public class PaymentRequest {
    private String orderId;
    private String orderName;
    private int amount;

    // 생성자
    public PaymentRequest() {
    }

    public PaymentRequest(String orderId, String orderName, int amount) {
        this.orderId = orderId;
        this.orderName = orderName;
        this.amount = amount;
    }

    // Getter / Setter
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderName() {
        return orderName;
    }

    public void setOrderName(String orderName) {
        this.orderName = orderName;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
