// io/goorm/team02/core/deliveries/controller/dto/AcceptRequest.java
package io.goorm.team02.core.deliveries.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AcceptRequest(@NotNull Long riderId) {}