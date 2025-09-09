// src/main/java/io/goorm/team02/core/payments/dto/PaymentResponse.java
package io.goorm.team02.core.payments.dto;

public class PaymentResponse {
    private String paymentId;
    private int amount;
    private String status;

    public PaymentResponse() {
    }

    public PaymentResponse(String paymentId, int amount, String status) {
        this.paymentId = paymentId;
        this.amount = amount;
        this.status = status;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
