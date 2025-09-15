// io/goorm/team02/core/deliveries/controller/dto/RejectRequest.java
package io.goorm.team02.core.deliveries.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record RejectRequest(@NotBlank String riderId, String reason) {}