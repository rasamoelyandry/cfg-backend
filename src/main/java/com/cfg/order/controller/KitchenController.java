package com.cfg.order.controller;

import com.cfg.common.dto.ApiResponse;
import com.cfg.order.dto.OrderResponse;
import com.cfg.order.dto.UpdateOrderStatusRequest;
import com.cfg.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/restaurants/{restaurantId}/kitchen")
@RequiredArgsConstructor
public class KitchenController {

    private final OrderService orderService;

    /**
     * Toutes les commandes actives pour le board cuisine (triées par heure de création).
     * Utilisé au chargement initial — ensuite WebSocket prend le relais.
     */
    @GetMapping("/board")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','OWNER','MANAGER','KITCHEN')")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getBoard(
            @PathVariable UUID restaurantId) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getActiveOrders(restaurantId)));
    }

    /**
     * Changement de statut depuis la cuisine (PENDING→PREPARING→READY).
     * Broadcast WebSocket automatique via OrderEventPublisher dans OrderService.
     */
    @PatchMapping("/orders/{orderId}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','OWNER','MANAGER','KITCHEN')")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable UUID restaurantId,
            @PathVariable UUID orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                orderService.updateStatus(restaurantId, orderId, request.getStatus())));
    }
}
