package io.goorm.team02.core.deliveries.domain;

import io.goorm.team02.core.common.domain.BaseEntity;
import io.goorm.team02.core.deliveries.domain.enums.DeliveryStatus;
import io.goorm.team02.core.orders.domain.Order;
import io.goorm.team02.core.users.domain.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "deliveries")
@lombok.Getter
@lombok.NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Delivery extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne @JoinColumn(name = "rider_id")
    private User rider;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String pickupAddress;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String deliveryAddress;

    @Column(precision = 5, scale = 2)
    private BigDecimal distanceKm;

    private Integer estimatedTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @lombok.Setter
    private DeliveryStatus status = DeliveryStatus.REQUESTED;

    @lombok.Setter private LocalDateTime acceptedAt;
    @lombok.Setter private LocalDateTime pickedUpAt;
    @lombok.Setter private LocalDateTime deliveredAt;

    @OneToMany(mappedBy = "delivery", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DeliveryLocation> locations;

    // 필요하면 requestedAt도 게터만 유지
    private LocalDateTime requestedAt = LocalDateTime.now();
}