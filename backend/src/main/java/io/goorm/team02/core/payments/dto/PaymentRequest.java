// src/main/java/com/example/toss/dto/PaymentRequest.java
package io.goorm.team02.core.payments.dto;

public class PaymentRequest {
    private String orderId;
    private String orderName;
    private int amount;

    // getters & setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getOrderName() { return orderName; }
    public void setOrderName(String orderName) { this.orderName = orderName; }

    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }
}
