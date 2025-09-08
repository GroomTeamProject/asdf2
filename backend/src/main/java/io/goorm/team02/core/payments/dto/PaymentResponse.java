// src/main/java/com/example/toss/dto/PaymentResponse.java
package io.goorm.team02.core.payments.dto;

public class PaymentResponse {
    private String paymentUrl;

    public PaymentResponse(String paymentUrl) {
        this.paymentUrl = paymentUrl;
    }

    public String getPaymentUrl() { return paymentUrl; }
    public void setPaymentUrl(String paymentUrl) { this.paymentUrl = paymentUrl; }
}
