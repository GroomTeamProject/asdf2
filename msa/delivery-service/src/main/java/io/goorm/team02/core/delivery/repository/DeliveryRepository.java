package io.goorm.team02.core.delivery.repository;

import io.goorm.team02.core.delivery.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRepository extends JpaRepository<Delivery,Long> {
}
