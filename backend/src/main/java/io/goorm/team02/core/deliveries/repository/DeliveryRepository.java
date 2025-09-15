// io.goorm.team02.core.deliveries.repository.DeliveryRepository
package io.goorm.team02.core.deliveries.repository;

import io.goorm.team02.core.deliveries.domain.Delivery;
import io.goorm.team02.core.deliveries.domain.enums.DeliveryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    Page<Delivery> findByStatus(DeliveryStatus status, Pageable pageable);
    Page<Delivery> findByRiderIdAndStatusIn(String riderId, Collection<DeliveryStatus> statuses, Pageable pageable);
    Page<Delivery> findByRiderIdAndStatus(String riderId, DeliveryStatus status, Pageable pageable);
}