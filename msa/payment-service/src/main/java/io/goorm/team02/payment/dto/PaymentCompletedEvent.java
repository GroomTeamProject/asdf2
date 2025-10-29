package io.goorm.team02.payment.event;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentCompletedEvent {
    private String paymentKey;
    private String orderId;
    private Long amount;
    private String status;
}
