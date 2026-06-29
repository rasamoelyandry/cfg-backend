package com.cfg.order.service;

import com.cfg.kitchen.websocket.OrderEventMessage;
import com.cfg.order.domain.Order;
import com.cfg.order.domain.OrderItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public void publishOrderEvent(String eventType, Order order) {
        OrderEventMessage message = buildMessage(eventType, order);
        String topic = "/topic/kitchen/" + order.getRestaurantId();
        messagingTemplate.convertAndSend(topic, message);
        log.debug("Published {} to {}", eventType, topic);
    }

    private OrderEventMessage buildMessage(String eventType, Order order) {
        return OrderEventMessage.builder()
                .eventType(eventType)
                .orderId(order.getId())
                .restaurantId(order.getRestaurantId())
                .tableId(order.getTableId())
                .status(order.getStatus().name())
                .customerName(order.getCustomerName())
                .totalAmount(order.getTotalAmount())
                .timestamp(Instant.now())
                .items(order.getItems().stream()
                        .map(this::toItemDto)
                        .collect(Collectors.toList()))
                .build();
    }

    private OrderEventMessage.ItemDto toItemDto(OrderItem item) {
        return OrderEventMessage.ItemDto.builder()
                .id(item.getId())
                .menuItemName(item.getMenuItemName())
                .quantity(item.getQuantity())
                .notes(item.getNotes())
                .status(item.getStatus().name())
                .build();
    }
}
