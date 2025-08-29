package io.goorm.team02.core.order.repository;

import io.goorm.team02.core.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

}
