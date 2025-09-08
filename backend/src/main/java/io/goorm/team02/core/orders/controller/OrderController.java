package io.goorm.team02.core.orders.controller;

import io.goorm.team02.core.orders.controller.dto.OrderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

	@PostMapping
	public String create(@RequestBody OrderRequest request) {
		// DB 저장 없이 결제 완료만 처리
		System.out.println("주문 데이터 확인: " + request); // 콘솔에서 데이터 확인 가능
		return "주문 및 결제 완료!";
	}
}
