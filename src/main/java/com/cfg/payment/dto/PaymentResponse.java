package com.cfg.payment.dto;

import com.cfg.payment.domain.Payment;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data @Builder
public class PaymentResponse {
    private UUID id;
    private UUID restaurantId;
    private UUID orderId;
    private String method;
    private BigDecimal amount;
    private String reference;
    private String notes;
    private Instant paidAt;

    public static PaymentResponse from(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId())
                .restaurantId(p.getRestaurantId())
                .orderId(p.getOrderId())
                .method(p.getMethod().name())
                .amount(p.getAmount())
                .reference(p.getReference())
                .notes(p.getNotes())
                .paidAt(p.getPaidAt())
                .build();
    }
}
