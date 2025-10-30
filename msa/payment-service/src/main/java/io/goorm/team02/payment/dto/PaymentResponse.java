package io.goorm.team02.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class PaymentResponse {
    private String orderId;
    private BigDecimal amount;
    private String status;
    private String paymentKey;
    private String pgProvider;
    private String paymentMethod;
    private String pgTid;
}
