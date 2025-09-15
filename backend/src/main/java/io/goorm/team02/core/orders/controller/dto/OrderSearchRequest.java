package io.goorm.team02.core.orders.controller.dto;

import io.goorm.team02.core.orders.domain.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSearchRequest {

    private Long storeId;
    private Long userId;
    private OrderStatus status;

    // 검증 메서드
    public boolean hasStoreId() {
        return storeId != null;
    }

    public boolean hasUserId() {
        return userId != null;
    }

    public boolean hasStatus() {
        return status != null;
    }

}
