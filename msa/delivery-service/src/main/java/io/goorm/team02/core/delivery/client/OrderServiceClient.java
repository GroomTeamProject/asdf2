package io.goorm.team02.core.delivery.client;


import io.goorm.team02.core.delivery.config.FeignClientConfig;
import io.goorm.team02.dto.orders.OrderResponse;
import io.goorm.team02.dto.orders.OrderResponseForDelivery;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

//  2025-11-24: GetMapping
@FeignClient(
        name = "order-service",
        url = "${msa.gateway.url}",
        configuration = FeignClientConfig.class
)
public interface OrderServiceClient {

    @GetMapping("/api/orders/delivery/{orderId}")
    OrderResponseForDelivery getOrderDetailForDelivery(@PathVariable("orderId") Long orderId);

    // pickup
    @PutMapping("/api/orders//{orderId}/start-delivery")
    OrderResponse startDelivery(@PathVariable Long orderId);

    // complete
    @PutMapping("/api/orders/{orderId}/deliver")
    OrderResponse deliverOrder(@PathVariable Long orderId);
}
