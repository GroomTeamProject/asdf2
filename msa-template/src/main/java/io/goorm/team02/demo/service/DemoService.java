package io.goorm.team02.demo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import io.goorm.team02.demo.client.OrderServiceClient;
import io.goorm.team02.demo.dto.OrderResponse;

@Service
@RequiredArgsConstructor
public class DemoService {

    private final OrderServiceClient orderServiceClient;

    /**
     * 주문 조회
     */
    public OrderResponse getOrderById(Long id) {
        return orderServiceClient.getOrderById(id);
    }

}
