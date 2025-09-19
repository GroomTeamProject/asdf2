package io.goorm.team02.core.orders.domain;

import io.goorm.team02.core.payments.domain.Payment;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerName;
    private String phoneNumber;
    private String address;
    private String requestMessage;
    private String orderMemo;
    private BigDecimal totalAmount;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<OrderItem> items;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private Payment payment;

    @Builder.Default
    private String status = "CREATED"; // 기본 생성 상태

    public boolean canCancel() {
        return "CREATED".equals(this.status); // 이미 완료되거나 취소된 주문은 취소 불가
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
