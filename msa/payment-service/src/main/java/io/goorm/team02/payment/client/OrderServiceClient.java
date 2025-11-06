package io.goorm.team02.payment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "order-service-client", url = "${msa.gateway.url:http://order-service:8085}")
public interface OrderServiceClient {

    @PostMapping("/api/orders/{orderNumber}/payment")
    void notifyPaymentResult(@PathVariable("orderNumber") String orderNumber,
                             @RequestBody PaymentResultDto result);

    class PaymentResultDto {
        public String status;
        public String transactionId;
        public Integer amount;
        public String message;
    }
}
