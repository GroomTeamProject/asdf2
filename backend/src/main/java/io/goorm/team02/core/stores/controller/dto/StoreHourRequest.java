package io.goorm.team02.core.stores.controller.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StoreHourRequest {

    private Integer dayOfWeek; // 0=일요일, 1=월요일, ..., 6=토요일

    @JsonDeserialize(using = LocalTimeObjectDeserializer.class)
    private LocalTime openTime;

    @JsonDeserialize(using = LocalTimeObjectDeserializer.class)
    private LocalTime closeTime;

    private Boolean isClosed;
}