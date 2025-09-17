package io.goorm.team02.core.orders.repository;

import io.goorm.team02.core.orders.domain.Order;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    //Optional<Order> findByOrderIdString(String orderIdString);

    //Optional<Order> findTopByOrderByCreatedAtDesc();

    Optional<Order> findTopByOrderByIdDesc();
}
