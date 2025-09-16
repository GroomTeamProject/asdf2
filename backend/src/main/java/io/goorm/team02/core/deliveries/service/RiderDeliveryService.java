// src/main/java/io/goorm/team02/core/deliveries/service/RiderDeliveryService.java
package io.goorm.team02.core.deliveries.service;

import io.goorm.team02.core.deliveries.domain.Delivery;
import io.goorm.team02.core.deliveries.domain.enums.DeliveryStatus;
import io.goorm.team02.core.deliveries.repository.DeliveryRepository;
import io.goorm.team02.core.users.domain.User;
import io.goorm.team02.core.users.repository.UserAddressRepository;
import io.goorm.team02.core.users.repository.UserinfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

// RiderDeliveryService.java
@Service
@RequiredArgsConstructor
@Transactional
public class RiderDeliveryService {
    private final DeliveryRepository repo;
    private final UserinfoRepository userInfoRepository;

    private Delivery get(Long id){ return repo.findById(id).orElseThrow(); }

    @Transactional
    public Delivery accept(Long id, String riderIdStr){
        Delivery d = get(id);
        if (d.getStatus() != DeliveryStatus.REQUESTED)
            throw new IllegalStateException("not REQUESTED");

        Long riderId;
        try { riderId = Long.valueOf(riderIdStr); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("invalid riderId: " + riderIdStr); }

        User rider = userInfoRepository.findById(riderId)
                .orElseThrow(() -> new IllegalArgumentException("rider not found: " + riderId));

        d.assignRider(rider);                 // 또는 d.setRider(rider)
        d.setStatus(DeliveryStatus.ACCEPTED);
        d.setAcceptedAt(LocalDateTime.now());
        return d;                             // 영속 상태면 save 불필요
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