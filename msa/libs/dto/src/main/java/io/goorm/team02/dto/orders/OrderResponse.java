package io.goorm.team02.dto.orders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        String orderNumber,
        Long userId,
        Long storeId,
        String deliveryAddress,
        String deliveryDetailAddress,
        String storeAddress,
        String storeDetailAddress,
        String phone,
        String orderMemo,
        BigDecimal menuTotalAmount,
        BigDecimal deliveryFee,
        BigDecimal discountAmount,
        BigDecimal totalAmount,
        String status,
        LocalDateTime orderedAt,
        LocalDateTime acceptedAt,
        LocalDateTime cookingStartedAt,
        LocalDateTime cookingCompletedAt,
        LocalDateTime pickedUpAt,
        LocalDateTime deliveredAt,
        LocalDateTime cancelledAt,
        String rejectReason,
        String cancelReason,
        Integer minCookingTime,
        Integer maxCookingTime,
        List<OrderItemResponse> orderItems) {

    public record OrderItemResponse(
            Long id,
            Long menuId,
            String menuName,
            BigDecimal menuPrice,
            Integer quantity,
            BigDecimal totalPrice,
            List<OrderItemOptionResponse> options) {
    }

    public record OrderItemOptionResponse(
            Long id,
            String optionName,
            String optionItemName,
            BigDecimal additionalPrice) {
    }
}