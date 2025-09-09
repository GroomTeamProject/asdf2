package io.goorm.team02.core.orders.repository;

import io.goorm.team02.core.orders.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o " +
           "LEFT JOIN FETCH o.user u " +
           "LEFT JOIN FETCH o.store s " +
           "WHERE o.store.id = :storeId " +
           "ORDER BY o.orderedAt DESC")
    List<Order> findAllByStoreIdWithDetails(@Param("storeId") Long storeId);

    List<Order> findAllByStoreId(Long storeId);

}
