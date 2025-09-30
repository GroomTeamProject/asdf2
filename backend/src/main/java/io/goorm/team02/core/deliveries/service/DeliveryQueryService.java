// io.goorm.team02.core.deliveries.service.DeliveryQueryService
package io.goorm.team02.core.deliveries.service;

import io.goorm.team02.core.deliveries.domain.Delivery;
import io.goorm.team02.core.deliveries.domain.enums.DeliveryStatus;
import io.goorm.team02.core.deliveries.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryQueryService {

    private final DeliveryRepository repo;

    /** 배달 요청 목록(REQUESTED) */
    public Page<Delivery> listRequested(Pageable pageable) {
        return repo.findByStatus(DeliveryStatus.REQUESTED, pageable);
    }

    // 진행 중(ACCEPTED, PICKED_UP)
    public Page<Delivery> listInProgress(String riderId, Pageable pageable) {
        return repo.findByRiderIdAndStatusIn(
                riderId,
                java.util.List.of(DeliveryStatus.ACCEPTED, DeliveryStatus.PICKED_UP),
                pageable
        );
    }

    // 완료 이력(DELIVERED)
    public Page<Delivery> history(String riderId, Pageable pageable) {
        return repo.findByRiderIdAndStatus(riderId, DeliveryStatus.DELIVERED, pageable);
    }

    /** 단건 조회 */
    public Optional<Delivery> get(Long id) {
        return repo.findById(id);
    }

    /**
     * 주어진 라이더의 오늘(자정 기준) 완료된 배달 건들의 배달 수수료 합계를 계산한다.
     *
     * @param riderId 라이더 ID
     * @return 오늘 완료된 배달들의 배달 수수료 합계 (없으면 0)
     */
    public Long getTodayDeliveredFee(Long riderId) {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();            // 오늘 00:00
        LocalDateTime end = today.plusDays(1).atStartOfDay();  // 내일 00:00

        Long sum = repo.sumFeeByRiderAndDate(
                riderId, start, end, DeliveryStatus.DELIVERED);

        return sum != null ? sum : 0L; // null 방지
    }

    public Long countByRiderId(Long riderId) {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();            // 오늘 00:00
        LocalDateTime end = today.plusDays(1).atStartOfDay();  // 내일 00:00

        Long cnt = repo.countByRiderId(
                riderId, start, end, DeliveryStatus.DELIVERED);
        return cnt != null ? cnt : 0L;
    }

    public Long AvgByRiderId(Long riderId) {

        Long avg = repo.findAvgDeliveryMinutes(riderId);
        return avg != null ? avg : 0L;
    }

    public DeliveryStatus getRiderStatus(Long riderId) {
        var st = List.of(DeliveryStatus.ACCEPTED, DeliveryStatus.PICKED_UP);
        return repo.findTop1ByRider_IdAndStatusInOrderByIdDesc(riderId, st)
                .map(Delivery::getStatus)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No in-progress delivery for riderId=" + riderId));
    }

    public Optional<Delivery> getDeliveryByRiderId(Long riderId) {
        var st = List.of(DeliveryStatus.ACCEPTED, DeliveryStatus.PICKED_UP);
        return repo.findTop1ByRider_IdAndStatusInOrderByIdDesc(riderId, st);
    }

}