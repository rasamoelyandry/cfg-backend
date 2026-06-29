package com.cfg.sync.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class SyncBatchRequest {
    @NotEmpty
    private List<SyncItem> events;

    @Data
    public static class SyncItem {
        private UUID clientUuid;
        private String eventType;
        private String payload;  // JSON string
        private Instant localTimestamp;
    }
}
