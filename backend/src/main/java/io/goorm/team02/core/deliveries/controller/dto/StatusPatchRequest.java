// StatusPatchRequest.java
package io.goorm.team02.core.deliveries.controller.dto;

import io.goorm.team02.core.deliveries.domain.enums.DeliveryStatus;
import jakarta.validation.constraints.NotNull;

/** 허용: PICKED_UP 또는 DELIVERED */
public record StatusPatchRequest(@NotNull DeliveryStatus status) {}