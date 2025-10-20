package io.goorm.team02.payment.client;

import io.goorm.team02.payment.dto.OrderEventResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "order-service", url = "${msa.gateway.url:http://localhost:8080}")
public interface PaymentServiceClient {
    @GetMapping("/api/orders/events/{orderId}")
    OrderEventResponse getOrderEvent(@PathVariable("orderId") String orderId);
}
