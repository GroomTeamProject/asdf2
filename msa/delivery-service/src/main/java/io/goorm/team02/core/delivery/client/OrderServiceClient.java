package io.goorm.team02.core.delivery.client;


import io.goorm.team02.dto.orders.OrderResponseForDelivery;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

//  2025-11-24
@FeignClient(name = "order-service", url = "${msa.gateway.url}")
public interface OrderServiceClient {

    @GetMapping("/delivery/{orderId}")
    OrderResponseForDelivery getOrderDetailForDelivery(@PathVariable("orderId") Long orderId);
}
