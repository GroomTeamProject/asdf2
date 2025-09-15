// io/goorm/team02/core/deliveries/controller/dto/DeliveryResponse.java
package io.goorm.team02.core.deliveries.controller.dto;

import io.goorm.team02.core.deliveries.domain.Delivery;
import java.time.LocalDateTime;

public record DeliveryResponse(
        Long deliveryId,
        Long orderId,
        String status,
        String riderId,
        String pickupAddress,
        String deliveryAddress,
        Integer estimatedTime,
        String distanceKm,
        LocalDateTime requestedAt,
        LocalDateTime acceptedAt,
        LocalDateTime pickedUpAt,
        LocalDateTime deliveredAt
) {
    public static DeliveryResponse of(Delivery d) {
        return new DeliveryResponse(
                d.getId(),
                d.getOrder().getId(),
                d.getStatus().name(),
                d.getRider()!=null ? String.valueOf(d.getRider().getId()) : null,
                d.getPickupAddress(),
                d.getDeliveryAddress(),
                d.getEstimatedTime(),
                d.getDistanceKm()!=null ? d.getDistanceKm().toPlainString() : null,
                d.getRequestedAt(),
                d.getAcceptedAt(),
                d.getPickedUpAt(),
                d.getDeliveredAt()
        );
    }
}