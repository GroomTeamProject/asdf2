// src/main/java/io/goorm/team02/core/deliveries/service/RiderDeliveryService.java
package io.goorm.team02.core.deliveries.service;

import io.goorm.team02.core.deliveries.domain.Delivery;
import io.goorm.team02.core.deliveries.domain.enums.DeliveryStatus;
import io.goorm.team02.core.deliveries.repository.DeliveryRepository;
import io.goorm.team02.core.orders.domain.Order;
import io.goorm.team02.core.orders.domain.enums.OrderStatus;
import io.goorm.team02.core.orders.repository.OrderRepository;
import io.goorm.team02.core.users.domain.User;
import io.goorm.team02.core.users.repository.UserinfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
// RiderDeliveryService.java
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RiderDeliveryService {
    private final DeliveryRepository repo;
    private final UserinfoRepository userInfoRepository;
    private final OrderRepository orderRepository;
    private Delivery get(Long id){ return repo.findById(id).orElseThrow(); }

    @Transactional
    public Delivery accept(Long orderId, Long riderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("order not found: " + orderId));
        User rider = userInfoRepository.findById(riderId)
                .orElseThrow(() -> new IllegalArgumentException("rider not found: " + riderId));

        Delivery delivery = repo.findByOrder_Id(orderId)
                .orElseGet(() -> Delivery.create(order, rider));  // get() 금지

        if (delivery.getStatus() != DeliveryStatus.REQUESTED
                && delivery.getStatus() != DeliveryStatus.ACCEPTED) {
            throw new IllegalStateException("Delivery cannot be accepted in current state");
        }

        order.setStatus(OrderStatus.ACCEPTED);
        delivery.setStatus(DeliveryStatus.ACCEPTED);
        delivery.setAcceptedAt(LocalDateTime.now());
        delivery.setDeliveryFee(order.getDeliveryFee());
        delivery.assignRider(rider);

        return repo.save(delivery);
    }




    public Delivery reject(Long id, String riderId, String reason){
        var d = get(id);
        if (d.getStatus()!=DeliveryStatus.REQUESTED) throw new IllegalStateException("not REQUESTED");
        d.setStatus(DeliveryStatus.CANCELLED);
        //d.getOrder().setStatus(OrderStatus.CANCELLED);
        return d;
    }

    public Delivery pickup(Long orderId){
        var d = repo.findByOrder_Id(orderId)
                .orElseThrow(() -> new IllegalArgumentException("delivery not found"));
        if (d.getStatus() != DeliveryStatus.ACCEPTED) {
            throw new IllegalStateException("not ACCEPTED");
        }
        d.setStatus(DeliveryStatus.PICKED_UP);
        d.getOrder().setStatus(OrderStatus.PICKED_UP);
        d.setPickedUpAt(LocalDateTime.now());
        return d;
    }

    public Delivery complete(Long orderId){
        var d = repo.findByOrder_Id(orderId)
                .orElseThrow(() -> new IllegalArgumentException("delivery not found"));
        if (d.getStatus() != DeliveryStatus.PICKED_UP) {
            throw new IllegalStateException("not PICKED_UP");
        }
        d.setStatus(DeliveryStatus.DELIVERED);
        d.setDeliveredAt(LocalDateTime.now());
        d.getOrder().setStatus(OrderStatus.DELIVERED);
        return d;
    }
}