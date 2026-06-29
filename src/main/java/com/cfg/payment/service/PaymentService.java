package com.cfg.payment.service;

import com.cfg.common.exception.BusinessException;
import com.cfg.common.exception.ResourceNotFoundException;
import com.cfg.common.exception.TenantAccessException;
import com.cfg.order.domain.Order;
import com.cfg.order.domain.OrderStatus;
import com.cfg.order.repository.OrderRepository;
import com.cfg.order.service.OrderEventPublisher;
import com.cfg.payment.domain.Payment;
import com.cfg.payment.dto.CreatePaymentRequest;
import com.cfg.payment.dto.PaymentResponse;
import com.cfg.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderEventPublisher eventPublisher;

    @Transactional
    public PaymentResponse createPayment(UUID restaurantId, UUID orderId,
                                         UUID userId, CreatePaymentRequest req) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        if (!order.getRestaurantId().equals(restaurantId)) throw new TenantAccessException();
        if (order.getStatus() == OrderStatus.PAID) throw new BusinessException("Order already paid");
        if (order.getStatus() == OrderStatus.CANCELLED) throw new BusinessException("Cannot pay a cancelled order");

        Payment payment = Payment.builder()
                .restaurantId(restaurantId)
                .orderId(orderId)
                .method(req.getMethod())
                .amount(req.getAmount())
                .reference(req.getReference())
                .notes(req.getNotes())
                .paidAt(Instant.now())
                .createdBy(userId)
                .build();

        paymentRepository.save(payment);

        order.setStatus(OrderStatus.PAID);
        order.setCompletedAt(Instant.now());
        Order saved = orderRepository.save(order);
        eventPublisher.publishOrderEvent("ORDER_PAID", saved);

        return PaymentResponse.from(payment);
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getPayments(UUID restaurantId, Pageable pageable) {
        return paymentRepository.findAllByRestaurantIdOrderByPaidAtDesc(restaurantId, pageable)
                .map(PaymentResponse::from);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getOrderPayments(UUID restaurantId, UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        if (!order.getRestaurantId().equals(restaurantId)) throw new TenantAccessException();
        return paymentRepository.findAllByOrderId(orderId).stream()
                .map(PaymentResponse::from).collect(Collectors.toList());
    }
}
