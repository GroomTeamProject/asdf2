package io.goorm.team02.core.delivery.client;


import io.goorm.team02.dto.orders.OrderResponseForDelivery;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "order-service", url = "${msa.gateway:http://localhost:8080}")
public interface OrderServiceClient {

    @GetMapping("/delivery/{orderId}")
    OrderResponseForDelivery getOrderDetailForDelivery(@PathVariable("orderId") Long orderId);
}
