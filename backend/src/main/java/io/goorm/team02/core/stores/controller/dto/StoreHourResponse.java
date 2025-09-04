package io.goorm.team02.core.stores.controller.dto;

import io.goorm.team02.core.stores.domain.StoreHour;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreHourResponse {
    private Long id;
    private Integer dayOfWeek;
    private LocalTime openTime;
    private LocalTime closeTime;
    private Boolean isClosed;

    // Entity -> DTO 변환
    public static StoreHourResponse from(StoreHour storeHour) {
        return StoreHourResponse.builder()
            .id(storeHour.getStore().getId())
            .dayOfWeek(storeHour.getDayOfWeek())
            .openTime(storeHour.getOpenTime())
            .closeTime(storeHour.getCloseTime())
            .isClosed(storeHour.getIsClosed())
            .build();
    }
}