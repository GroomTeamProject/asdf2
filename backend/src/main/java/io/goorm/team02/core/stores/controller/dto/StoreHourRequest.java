package io.goorm.team02.core.stores.controller.dto;


import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StoreHourRequest {

    private Integer dayOfWeek; // 0=일요일, 1=월요일, ..., 6=토요일
    private LocalTime openTime;
    private LocalTime closeTime;
    private Boolean isClosed;

    /**
     * 휴무일 설정용 생성자
     */
    public static StoreHourRequest createClosedDay(Integer dayOfWeek) {
        StoreHourRequest request = new StoreHourRequest();
        request.dayOfWeek = dayOfWeek;
        request.isClosed = true;
        return request;
    }

    /**
     * 영업일 설정용 생성자
     */
    public static StoreHourRequest createOpenDay(Integer dayOfWeek, LocalTime openTime, LocalTime closeTime) {
        StoreHourRequest request = new StoreHourRequest();
        request.dayOfWeek = dayOfWeek;
        request.openTime = openTime;
        request.closeTime = closeTime;
        request.isClosed = false;
        return request;
    }
}