package io.goorm.team02.payment.repository;

import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Repository
public class PaymentRepository {

    private final Map<String, String> paidOrders = new HashMap<>();

    public boolean existsByOrderId(String orderId) {
        return paidOrders.containsKey(orderId);
    }

    public void savePayment(String orderId, String userId, BigDecimal amount, String paymentKey, String pgProvider, String paymentMethod) {
        paidOrders.put(orderId, userId);
    }
}
