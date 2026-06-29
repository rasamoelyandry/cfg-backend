package com.cfg.payment.repository;

import com.cfg.payment.domain.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findAllByOrderId(UUID orderId);
    Page<Payment> findAllByRestaurantIdOrderByPaidAtDesc(UUID restaurantId, Pageable pageable);
}
