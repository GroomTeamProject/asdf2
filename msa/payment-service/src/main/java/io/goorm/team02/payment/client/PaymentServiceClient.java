package io.goorm.team02.payment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(name = "order-service", url = "${msa.gateway.url:http://localhost:8080}")
public interface PaymentServiceClient {

    @GetMapping("/api/orders/events/{orderId}")
    default OrderEventResponse getOrderEvent(@PathVariable("orderId") String orderId) {
        // 임시 더미 반환
        return new OrderEventResponse(orderId, "CREATED");
    }

    class OrderEventResponse {
        private String orderId;
        private String status;

        public OrderEventResponse(String orderId, String status) {
            this.orderId = orderId;
            this.status = status;
        }

        public String getOrderId() { return orderId; }
        public String getStatus() { return status; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        public void setStatus(String status) { this.status = status; }
    }
}
