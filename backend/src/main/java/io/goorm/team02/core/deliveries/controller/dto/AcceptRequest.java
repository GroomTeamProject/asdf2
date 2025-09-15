// io/goorm/team02/core/deliveries/controller/dto/AcceptRequest.java
package io.goorm.team02.core.deliveries.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record AcceptRequest(@NotBlank String riderId) {}