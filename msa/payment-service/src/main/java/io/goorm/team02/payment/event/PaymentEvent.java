package io.goorm.team02.payment.event;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent {
    private String paymentKey;
    private String orderId;
    private BigDecimal amount;
    private String method;
    private String status;
    private String pgProvider;
}
