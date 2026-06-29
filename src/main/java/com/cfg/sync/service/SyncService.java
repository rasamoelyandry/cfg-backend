package com.cfg.sync.service;

import com.cfg.common.security.UserPrincipal;
import com.cfg.order.dto.CreateOrderRequest;
import com.cfg.order.service.OrderService;
import com.cfg.sync.domain.SyncEvent;
import com.cfg.sync.domain.SyncStatus;
import com.cfg.sync.dto.SyncBatchRequest;
import com.cfg.sync.repository.SyncEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncService {

    private final SyncEventRepository syncEventRepository;
    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    @Transactional
    public List<Map<String, Object>> processBatch(UserPrincipal principal,
                                                   SyncBatchRequest request) {
        List<Map<String, Object>> results = new ArrayList<>();

        for (SyncBatchRequest.SyncItem item : request.getEvents()) {
            Map<String, Object> result = new HashMap<>();
            result.put("clientUuid", item.getClientUuid());

            // Idempotence : skip si déjà traité
            if (syncEventRepository.existsByClientUuid(item.getClientUuid())) {
                result.put("status", "ALREADY_PROCESSED");
                results.add(result);
                continue;
            }

            SyncEvent event = SyncEvent.builder()
                    .clientUuid(item.getClientUuid())
                    .restaurantId(principal.getRestaurantId())
                    .userId(principal.getId())
                    .eventType(item.getEventType())
                    .payload(item.getPayload())
                    .localTimestamp(item.getLocalTimestamp() != null ? item.getLocalTimestamp() : Instant.now())
                    .build();

            try {
                Object serverResponse = processEvent(principal, item);
                event.setStatus(SyncStatus.PROCESSED);
                event.setProcessedAt(Instant.now());
                result.put("status", "PROCESSED");
                result.put("serverResponse", serverResponse);
            } catch (Exception e) {
                log.warn("Sync error for clientUuid={}: {}", item.getClientUuid(), e.getMessage());
                event.setStatus(SyncStatus.ERROR);
                event.setErrorMessage(e.getMessage());
                result.put("status", "ERROR");
                result.put("error", e.getMessage());
            }

            syncEventRepository.save(event);
            results.add(result);
        }

        return results;
    }

    private Object processEvent(UserPrincipal principal, SyncBatchRequest.SyncItem item) throws Exception {
        return switch (item.getEventType()) {
            case "CREATE_ORDER" -> {
                CreateOrderRequest req = objectMapper.readValue(item.getPayload(), CreateOrderRequest.class);
                yield orderService.createOrder(principal.getRestaurantId(), principal.getId(), req);
            }
            default -> throw new IllegalArgumentException("Unknown event type: " + item.getEventType());
        };
    }
}
