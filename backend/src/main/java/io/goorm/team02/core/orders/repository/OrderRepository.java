package io.goorm.team02.core.orders.repository;

import io.goorm.team02.core.orders.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
