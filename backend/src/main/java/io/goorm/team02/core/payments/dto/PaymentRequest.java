package io.goorm.team02.core.payments.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PaymentRequest {
    private String paymentKey;
    private String pgProvider;
    private String pgTid;
    private BigDecimal amount;
}
