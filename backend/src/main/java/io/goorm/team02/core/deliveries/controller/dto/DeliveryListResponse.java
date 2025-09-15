// io/goorm/team02/core/deliveries/controller/dto/DeliveryListResponse.java
package io.goorm.team02.core.deliveries.controller.dto;

import org.springframework.data.domain.Page;
import java.util.List;

public record DeliveryListResponse<T>(List<T> items, Pagination pagination) {
    public static <T> DeliveryListResponse<T> of(Page<T> page) {
        return new DeliveryListResponse<>(
                page.getContent(),
                new Pagination(page.getNumber()+1, page.getSize(), page.getTotalElements(), page.hasNext())
        );
    }
    public record Pagination(int page, int limit, long total, boolean hasNext) {}
}