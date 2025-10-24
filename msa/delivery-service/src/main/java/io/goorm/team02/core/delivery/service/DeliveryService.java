package io.goorm.team02.core.delivery.service;

import io.goorm.team02.core.delivery.entity.enums.DeliveryStatus;
import io.goorm.team02.core.delivery.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DeliveryService {
    DeliveryRepository deliveryRepository;

    public Long getTodayCount(Long riderId) {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        Long count = deliveryRepository.countByRiderId(riderId,start,end, DeliveryStatus.ACCEPTED.name());

        return count != null ? count : 0;
    }

    public Long getTodayIncome(Long riderId) {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        Long income = deliveryRepository.sumFeeByRiderAndDate(riderId,
                start,end, DeliveryStatus.DELIVERED.name());
        return income != null ? income : 0;
    }

    public Long getTodayAvg(Long riderId) {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        Long avg = deliveryRepository.findAvgDeliveryMinutes(riderId,start,end, DeliveryStatus.DELIVERED.name());
        return avg != null ? avg : 0;
    }
    public String getRiderStatus(Long riderId) {
        long cnt = Optional.ofNullable(deliveryRepository.notDeliveredCountByRiderId(riderId))
                .orElse(0L);
        return cnt > 0 ? "BAD" : "GOOD";
    }

}