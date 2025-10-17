package io.goorm.team02.core.owner.stores.controller.dto.ordermanagement;

import io.goorm.team02.core.owner.stores.domain.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record StoreOrderDetailResponse(
        Long orderId,
        String orderNumber,
        String customerName,
        String customerPhone,
        String deliveryAddress,
        String deliveryDetailAddress,
        String orderMemo,
        OrderStatus status,
        BigDecimal menuTotalAmount,
        BigDecimal deliveryFee,
        BigDecimal totalAmount,
        LocalDateTime orderedAt,
        LocalDateTime acceptedAt,
        Integer minCookingTime,
        Integer maxCookingTime,
        String rejectReason,
        List<StoreOrderItemDetailResponse> orderItems
) {}
