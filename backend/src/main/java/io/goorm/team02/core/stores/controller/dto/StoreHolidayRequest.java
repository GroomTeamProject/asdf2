package io.goorm.team02.core.stores.controller.dto;

import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StoreHolidayRequest {

    private LocalDate date;
    private String reason;
    private Boolean isRecurring; // 매년 반복 여부
}