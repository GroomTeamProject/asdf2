package io.goorm.team02.core.delivery.mapper;

import io.goorm.team02.core.delivery.entity.Delivery;
import io.goorm.team02.dto.deliveries.DeliveryResponse;

public class DeliveryMapper {

    public static DeliveryResponse toResponse(Delivery d) {
        return new DeliveryResponse(
                d.getId(),
                d.getOrderId(),
                d.getRiderId(),
                d.getPickupAddress(),
                d.getDeliveryAddress(),
                d.getEstimatedTime(),
                d.getDistanceKm(),
                d.getAcceptedAt(),
                d.getPickedUpAt(),
                d.getDeliveredAt(),
                d.getDeliveryFee()
        );
    }
}