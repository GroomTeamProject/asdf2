package io.goorm.team02.payment.client;

import io.goorm.team02.dto.orders.OrderResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class PaymentServiceClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String ORDER_SERVICE_URL = "http://localhost:8080/api/orders/";

    public OrderResponse getOrder(String orderId) {
        return restTemplate.getForObject(ORDER_SERVICE_URL + "events/" + orderId, OrderResponse.class);
    }
}
