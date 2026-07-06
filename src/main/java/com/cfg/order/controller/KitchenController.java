package com.cfg.order.controller;

import com.cfg.common.dto.ApiResponse;
import com.cfg.order.domain.OrderStatus;
import com.cfg.order.dto.OrderResponse;
import com.cfg.order.dto.UpdateOrderStatusRequest;
import com.cfg.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/restaurants/{restaurantId}/kitchen")
@RequiredArgsConstructor
public class KitchenController {

    private final OrderService orderService;

    private static final List<OrderStatus> BOARD_COLUMNS =
            List.of(OrderStatus.PENDING, OrderStatus.PREPARING, OrderStatus.READY);

    /**
     * Commandes actives groupées par statut ({PENDING, PREPARING, READY}) pour les 3 colonnes du board cuisine.
     * Utilisé au chargement initial — ensuite WebSocket prend le relais.
     */
    @GetMapping("/board")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','OWNER','MANAGER','KITCHEN')")
    public ResponseEntity<ApiResponse<Map<String, List<OrderResponse>>>> getBoard(
            @PathVariable UUID restaurantId) {
        List<OrderResponse> active = orderService.getActiveOrders(restaurantId);

        Map<String, List<OrderResponse>> board = new LinkedHashMap<>();
        for (OrderStatus status : BOARD_COLUMNS) {
            board.put(status.name(), new ArrayList<>());
        }
        for (OrderResponse order : active) {
            List<OrderResponse> column = board.get(order.getStatus());
            if (column != null) {
                column.add(order);
            }
        }
        return ResponseEntity.ok(ApiResponse.ok(board));
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
