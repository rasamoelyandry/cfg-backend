package com.cfg.order.controller;

import com.cfg.common.dto.ApiResponse;
import com.cfg.common.dto.PageResponse;
import com.cfg.common.security.UserPrincipal;
import com.cfg.order.domain.OrderStatus;
import com.cfg.order.dto.*;
import com.cfg.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/restaurants/{restaurantId}/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','OWNER','MANAGER','WAITER')")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getOrders(
            @PathVariable UUID restaurantId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(
                orderService.getOrders(restaurantId, status, pageable))));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','OWNER','MANAGER','WAITER','KITCHEN')")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getActiveOrders(
            @PathVariable UUID restaurantId) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getActiveOrders(restaurantId)));
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','OWNER','MANAGER','WAITER','KITCHEN')")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
            @PathVariable UUID restaurantId,
            @PathVariable UUID orderId) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getOrder(restaurantId, orderId)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','OWNER','MANAGER','WAITER')")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @PathVariable UUID restaurantId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateOrderRequest request) {
        OrderResponse created = orderService.createOrder(restaurantId, principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(created));
    }

    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','OWNER','MANAGER','WAITER','KITCHEN')")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable UUID restaurantId,
            @PathVariable UUID orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                orderService.updateStatus(restaurantId, orderId, request.getStatus())));
    }

    @PostMapping("/{orderId}/transfer")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','OWNER','MANAGER','WAITER')")
    public ResponseEntity<ApiResponse<OrderResponse>> transfer(
            @PathVariable UUID restaurantId,
            @PathVariable UUID orderId,
            @Valid @RequestBody TransferOrderRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                orderService.transferOrder(restaurantId, orderId, request)));
    }

    @PostMapping("/{orderId}/items")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','OWNER','MANAGER','WAITER')")
    public ResponseEntity<ApiResponse<OrderResponse>> addItem(
            @PathVariable UUID restaurantId,
            @PathVariable UUID orderId,
            @RequestBody CreateOrderRequest.OrderItemRequest itemRequest) {
        return ResponseEntity.ok(ApiResponse.ok(
                orderService.addItem(restaurantId, orderId, itemRequest)));
    }

    @DeleteMapping("/{orderId}/items/{itemId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','OWNER','MANAGER','WAITER')")
    public ResponseEntity<ApiResponse<OrderResponse>> removeItem(
            @PathVariable UUID restaurantId,
            @PathVariable UUID orderId,
            @PathVariable UUID itemId) {
        return ResponseEntity.ok(ApiResponse.ok(
                orderService.removeItem(restaurantId, orderId, itemId)));
    }

    /**
     * Masque la commande du tableau admin sans la supprimer de la base (conservée pour la comptabilité).
     */
    @DeleteMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','OWNER','MANAGER')")
    public ResponseEntity<ApiResponse<Void>> hideOrder(
            @PathVariable UUID restaurantId,
            @PathVariable UUID orderId) {
        orderService.hideOrder(restaurantId, orderId);
        return ResponseEntity.ok(ApiResponse.ok("Commande masquée"));
    }
}
