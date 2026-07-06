package com.cfg.order.service;

import com.cfg.common.exception.BusinessException;
import com.cfg.common.exception.ResourceNotFoundException;
import com.cfg.common.exception.TenantAccessException;
import com.cfg.menu.domain.MenuItem;
import com.cfg.menu.domain.MenuItemModifier;
import com.cfg.menu.repository.MenuItemRepository;
import com.cfg.order.domain.*;
import com.cfg.order.dto.CreateOrderRequest;
import com.cfg.order.dto.OrderResponse;
import com.cfg.order.dto.TransferOrderRequest;
import com.cfg.order.repository.OrderRepository;
import com.cfg.table.repository.TableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private final TableRepository tableRepository;
    private final OrderEventPublisher eventPublisher;

    @Transactional
    public OrderResponse createOrder(UUID restaurantId, UUID waiterId, CreateOrderRequest req) {
        // Idempotence offline sync
        if (req.getClientUuid() != null) {
            Optional<Order> existing = orderRepository.findByClientUuid(req.getClientUuid());
            if (existing.isPresent()) {
                return OrderResponse.from(existing.get());
            }
        }

        if (req.getTableId() != null) {
            markTableOccupied(req.getTableId(), restaurantId);
        }

        Order order = Order.builder()
                .restaurantId(restaurantId)
                .tableId(req.getTableId())
                .waiterId(waiterId)
                .customerName(req.getCustomerName())
                .notes(req.getNotes())
                .clientUuid(req.getClientUuid())
                .status(OrderStatus.PENDING)
                .sentToKitchenAt(Instant.now())
                .build();

        List<OrderItem> items = buildOrderItems(req.getItems(), restaurantId, order);
        order.setItems(items);
        order.recalculateTotal();

        Order saved = orderRepository.save(order);
        eventPublisher.publishOrderEvent("ORDER_CREATED", saved);

        return OrderResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrders(UUID restaurantId, OrderStatus status, Pageable pageable) {
        Page<Order> page = status != null
                ? orderRepository.findAllByRestaurantIdAndStatusAndHiddenFalseOrderByCreatedAtDesc(restaurantId, status, pageable)
                : orderRepository.findAllByRestaurantIdAndHiddenFalseOrderByCreatedAtDesc(restaurantId, pageable);
        return page.map(OrderResponse::from);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getActiveOrders(UUID restaurantId) {
        return orderRepository.findActiveOrdersByRestaurant(restaurantId)
                .stream().map(OrderResponse::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(UUID restaurantId, UUID orderId) {
        Order order = findOrderInRestaurant(restaurantId, orderId);
        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse updateStatus(UUID restaurantId, UUID orderId, OrderStatus newStatus) {
        Order order = findOrderInRestaurant(restaurantId, orderId);
        validateStatusTransition(order.getStatus(), newStatus);

        order.setStatus(newStatus);
        if (newStatus == OrderStatus.PAID || newStatus == OrderStatus.CANCELLED) {
            order.setCompletedAt(Instant.now());
        }
        List<OrderItem> itemsToRestore = newStatus == OrderStatus.CANCELLED
                ? new ArrayList<>(order.getItems())
                : List.of();

        Order saved = orderRepository.save(order);
        // Restitution du stock apres la sauvegarde : la requete bulk (clearAutomatically)
        // detache le contexte de persistance.
        itemsToRestore.forEach(i -> menuItemRepository.restoreStock(i.getMenuItemId(), i.getQuantity()));
        eventPublisher.publishOrderEvent("ORDER_STATUS_CHANGED", saved);
        return OrderResponse.from(saved);
    }

    @Transactional
    public OrderResponse transferOrder(UUID restaurantId, UUID orderId, TransferOrderRequest req) {
        Order order = findOrderInRestaurant(restaurantId, orderId);
        markTableOccupied(req.getTargetTableId(), restaurantId);

        if (order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessException("Cannot transfer a closed order");
        }

        order.setTableId(req.getTargetTableId());
        Order saved = orderRepository.save(order);
        eventPublisher.publishOrderEvent("ORDER_TRANSFERRED", saved);
        return OrderResponse.from(saved);
    }

    @Transactional
    public OrderResponse addItem(UUID restaurantId, UUID orderId, CreateOrderRequest.OrderItemRequest itemReq) {
        Order order = findOrderInRestaurant(restaurantId, orderId);
        requireNotYetInKitchen(order);

        List<OrderItem> newItems = buildOrderItems(List.of(itemReq), restaurantId, order);
        order.getItems().addAll(newItems);
        order.recalculateTotal();

        Order saved = orderRepository.save(order);
        eventPublisher.publishOrderEvent("ORDER_ITEM_ADDED", saved);
        return OrderResponse.from(saved);
    }

    @Transactional
    public OrderResponse removeItem(UUID restaurantId, UUID orderId, UUID itemId) {
        Order order = findOrderInRestaurant(restaurantId, orderId);
        requireNotYetInKitchen(order);

        OrderItem removed = order.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElse(null);

        order.getItems().removeIf(i -> i.getId().equals(itemId));
        order.recalculateTotal();
        Order saved = orderRepository.save(order);

        // Restitution du stock en dernier : la requete bulk (clearAutomatically) detache le
        // contexte de persistance, donc tout acces a une collection lazy non chargee doit se
        // faire avant cet appel.
        if (removed != null) {
            menuItemRepository.restoreStock(removed.getMenuItemId(), removed.getQuantity());
        }

        return OrderResponse.from(saved);
    }

    private void requireNotYetInKitchen(Order order) {
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("La commande a déjà été prise en charge par la cuisine, elle ne peut plus être modifiée par le serveur");
        }
    }

    @Transactional
    public void hideOrder(UUID restaurantId, UUID orderId) {
        Order order = findOrderInRestaurant(restaurantId, orderId);
        order.setHidden(true);
        orderRepository.save(order);
    }

    // --- Private helpers ---

    private Order findOrderInRestaurant(UUID restaurantId, UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        if (!order.getRestaurantId().equals(restaurantId)) {
            throw new TenantAccessException();
        }
        return order;
    }

    private void markTableOccupied(UUID tableId, UUID restaurantId) {
        var table = tableRepository.findById(tableId)
                .orElseThrow(() -> new ResourceNotFoundException("Table", tableId));
        if (!table.getRestaurantId().equals(restaurantId)) {
            throw new TenantAccessException("Table does not belong to this restaurant");
        }
        if (!table.isOccupied()) {
            table.setOccupied(true);
            tableRepository.save(table);
        }
    }

    private List<OrderItem> buildOrderItems(List<CreateOrderRequest.OrderItemRequest> requests, UUID restaurantId, Order order) {
        return requests.stream().map(req -> {
            MenuItem menuItem = menuItemRepository.findById(req.getMenuItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("MenuItem", req.getMenuItemId()));
            if (!menuItem.getRestaurantId().equals(restaurantId)) {
                throw new TenantAccessException("Menu item does not belong to this restaurant");
            }

            // Materialiser les modifiers pendant que menuItem est encore attache,
            // avant que decrementStock (bulk update, clearAutomatically) ne vide le contexte de persistance.
            Map<UUID, MenuItemModifier> modMap = menuItem.getModifiers().stream()
                    .collect(Collectors.toMap(MenuItemModifier::getId, m -> m));

            int quantity = Math.max(1, req.getQuantity());
            if (menuItem.isTrackStock()) {
                int updated = menuItemRepository.decrementStock(menuItem.getId(), quantity);
                if (updated == 0) {
                    throw new BusinessException("Stock insuffisant pour " + menuItem.getName());
                }
            }

            OrderItem item = OrderItem.builder()
                    .order(order)
                    .menuItemId(menuItem.getId())
                    .menuItemName(menuItem.getName())
                    .unitPrice(menuItem.getPrice())
                    .quantity(quantity)
                    .notes(req.getNotes())
                    .build();

            List<OrderItemModifier> modifiers = new ArrayList<>();
            if (req.getModifierIds() != null) {
                req.getModifierIds().forEach(modId -> {
                    MenuItemModifier mod = modMap.get(modId);
                    if (mod != null) {
                        modifiers.add(OrderItemModifier.builder()
                                .orderItem(item)
                                .modifierName(mod.getName())
                                .priceDelta(mod.getPriceDelta())
                                .build());
                    }
                });
            }
            item.setModifiers(modifiers);

            return item;
        }).collect(Collectors.toList());
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        Set<OrderStatus> allowed = switch (current) {
            case DRAFT -> Set.of(OrderStatus.PENDING, OrderStatus.CANCELLED);
            case PENDING -> Set.of(OrderStatus.PREPARING, OrderStatus.CANCELLED);
            case PREPARING -> Set.of(OrderStatus.READY, OrderStatus.CANCELLED);
            case READY -> Set.of(OrderStatus.SERVED, OrderStatus.PAID);
            case SERVED -> Set.of(OrderStatus.PAID);
            case PAID, CANCELLED -> Set.of();
        };
        if (!allowed.contains(next)) {
            throw new BusinessException("Invalid status transition: " + current + " → " + next);
        }
    }
}
