// io.goorm.team02.core.deliveries.repository.DeliveryRepository
package io.goorm.team02.core.deliveries.repository;

import io.goorm.team02.core.deliveries.domain.Delivery;
import io.goorm.team02.core.deliveries.domain.enums.DeliveryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    Page<Delivery> findByStatus(DeliveryStatus status, Pageable pageable);

    Page<Delivery> findByRiderIdAndStatusIn(String riderId, Collection<DeliveryStatus> statuses, Pageable pageable);

    Page<Delivery> findByRiderIdAndStatus(String riderId, DeliveryStatus status, Pageable pageable);

    @Query("SELECT COUNT (d.id) FROM Delivery d " +
            "WHERE d.rider.id = :riderId " +
            "AND d.createdAt >= :start " +
            "AND d.createdAt < :end " +
            "AND d.status = :status")
    Long countByRiderId(@Param("riderId") Long riderId,
                              @Param("start") LocalDateTime start,
                              @Param("end") LocalDateTime end,
                              @Param("status") DeliveryStatus status);


    @Query("SELECT SUM(d.deliveryFee) FROM Delivery d " +
            "WHERE d.rider.id = :riderId " +
            "AND d.createdAt >= :start " +
            "AND d.createdAt < :end " +
            "AND d.status = :status")
    Long sumFeeByRiderAndDate(@Param("riderId") Long riderId,
                              @Param("start") LocalDateTime start,
                              @Param("end") LocalDateTime end,
                              @Param("status") DeliveryStatus status);

    @Query(
            value = "SELECT FLOOR(AVG(TIMESTAMPDIFF(MINUTE, accepted_at, delivered_at))) " +
                    "FROM deliveries " +
                    "WHERE status = 'DELIVERED' " +
                    "AND rider_id = :riderId",
            nativeQuery = true
    )
    Long findAvgDeliveryMinutes(@Param("riderId") Long riderId);

    Optional<Delivery> findByOrder_Id(Long orderId); // 주문으로 배달 조회
}