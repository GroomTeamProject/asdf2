package io.goorm.team02.payment.dto;

import java.math.BigDecimal;

public class PaymentResponse {

    private String orderId;
    private BigDecimal amount;
    private String status;

    private String paymentKey;
    private String pgProvider;
    private String paymentMethod;
    private String pgTid;

    public PaymentResponse() {
    }

    public PaymentResponse(String orderId, BigDecimal amount, String status,
                           String paymentKey, String pgProvider,
                           String paymentMethod, String pgTid) {
        this.orderId = orderId;
        this.amount = amount;
        this.status = status;
        this.paymentKey = paymentKey;
        this.pgProvider = pgProvider;
        this.paymentMethod = paymentMethod;
        this.pgTid = pgTid;
    }

    // --- getters ---
    public String getOrderId() {
        return orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public String getPaymentKey() {
        return paymentKey;
    }

    public String getPgProvider() {
        return pgProvider;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getPgTid() {
        return pgTid;
    }

    // --- setters ---
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setPaymentKey(String paymentKey) {
        this.paymentKey = paymentKey;
    }

    public void setPgProvider(String pgProvider) {
        this.pgProvider = pgProvider;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void setPgTid(String pgTid) {
        this.pgTid = pgTid;
    }

    // --- convenience toString ---
    @Override
    public String toString() {
        return "PaymentResponse{" +
                "orderId='" + orderId + '\'' +
                ", amount=" + amount +
                ", status='" + status + '\'' +
                ", paymentKey='" + paymentKey + '\'' +
                ", pgProvider='" + pgProvider + '\'' +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", pgTid='" + pgTid + '\'' +
                '}';
    }
}
