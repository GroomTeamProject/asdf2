package io.goorm.team02.core.orders.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemOptionRequest {
    private Long optionId;
    private int quantity;
}
