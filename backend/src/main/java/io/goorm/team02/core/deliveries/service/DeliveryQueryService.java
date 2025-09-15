// io.goorm.team02.core.deliveries.service.DeliveryQueryService
package io.goorm.team02.core.deliveries.service;

import io.goorm.team02.core.deliveries.domain.Delivery;
import io.goorm.team02.core.deliveries.domain.enums.DeliveryStatus;
import io.goorm.team02.core.deliveries.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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
}