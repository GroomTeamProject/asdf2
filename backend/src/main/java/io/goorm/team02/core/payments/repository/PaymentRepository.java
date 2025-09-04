package io.goorm.team02.core.payments.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.goorm.team02.core.payments.domain.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
