package io.goorm.team02.core.delivery.client;


import io.goorm.team02.core.delivery.config.FeignClientConfig;
import io.goorm.team02.dto.orders.OrderResponseForDelivery;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

//  2025-11-24: GetMapping
@FeignClient(
        name = "order-service",
        url = "${msa.gateway.url}",
        configuration = FeignClientConfig.class
)
public interface OrderServiceClient {

    @GetMapping("/api/orders/delivery/{orderId}")
    OrderResponseForDelivery getOrderDetailForDelivery(@PathVariable("orderId") Long orderId);
}
