package io.goorm.team02.core.orders.controller;

import io.goorm.team02.core.orders.controller.dto.OrderRequest;
import io.goorm.team02.core.orders.controller.dto.OrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "주문 관리", description = "주문 관련 API")
public interface OrderControllerDocs {

	@Operation(summary = "주문 생성", description = "새로운 주문을 생성합니다")
	public OrderResponse create(@RequestBody OrderRequest order);

	@Operation(summary = "주문 목록 조회", description = "가게별 주문 목록을 조회합니다")
	public List<OrderResponse> getAll(
			@Parameter(description = "가게 ID", required = true) @RequestParam("storeId") Long storeId);

}