package com.cfg.payment.controller;

import com.cfg.common.dto.ApiResponse;
import com.cfg.common.dto.PageResponse;
import com.cfg.common.security.UserPrincipal;
import com.cfg.payment.dto.CreatePaymentRequest;
import com.cfg.payment.dto.PaymentResponse;
import com.cfg.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/api/v1/restaurants/{restaurantId}/orders/{orderId}/payment")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','OWNER','MANAGER','WAITER')")
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(
            @PathVariable UUID restaurantId,
            @PathVariable UUID orderId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreatePaymentRequest request) {
        PaymentResponse result = paymentService.createPayment(restaurantId, orderId, principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(result));
    }

    @GetMapping("/api/v1/restaurants/{restaurantId}/payments")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','OWNER','MANAGER')")
    public ResponseEntity<ApiResponse<PageResponse<PaymentResponse>>> getPayments(
            @PathVariable UUID restaurantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                PageResponse.of(paymentService.getPayments(restaurantId, PageRequest.of(page, size)))));
    }
}
