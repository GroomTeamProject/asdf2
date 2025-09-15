// src/main/java/io/goorm/team02/core/deliveries/service/RiderDeliveryService.java
package io.goorm.team02.core.deliveries.service;

import io.goorm.team02.core.deliveries.domain.Delivery;
import io.goorm.team02.core.deliveries.domain.enums.DeliveryStatus;
import io.goorm.team02.core.deliveries.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

// RiderDeliveryService.java
@Service
@RequiredArgsConstructor
@Transactional
public class RiderDeliveryService {
    private final DeliveryRepository repo;

    private Delivery get(Long id){ return repo.findById(id).orElseThrow(); }

    public Delivery accept(Long id, String riderId){
        var d = get(id);
        if (d.getStatus()!=DeliveryStatus.REQUESTED) throw new IllegalStateException("not REQUESTED");
        d.setStatus(DeliveryStatus.ACCEPTED);
        // d.setRider(...); // riderId 매핑 필요 시
        d.setAcceptedAt(LocalDateTime.now());
        return d;
    }

    public Delivery reject(Long id, String riderId, String reason){
        var d = get(id);
        if (d.getStatus()!=DeliveryStatus.REQUESTED) throw new IllegalStateException("not REQUESTED");
        d.setStatus(DeliveryStatus.CANCELLED);
        return d;
    }

    public Delivery pickup(Long id){
        var d = get(id);
        if (d.getStatus()!=DeliveryStatus.ACCEPTED) throw new IllegalStateException("not ACCEPTED");
        d.setStatus(DeliveryStatus.PICKED_UP);
        d.setPickedUpAt(LocalDateTime.now());
        return d;
    }

    public Delivery complete(Long id){
        var d = get(id);
        if (d.getStatus()!=DeliveryStatus.PICKED_UP) throw new IllegalStateException("not PICKED_UP");
        d.setStatus(DeliveryStatus.DELIVERED);
        d.setDeliveredAt(LocalDateTime.now());
        return d;
    }
}