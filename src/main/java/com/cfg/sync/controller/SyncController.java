package com.cfg.sync.controller;

import com.cfg.common.dto.ApiResponse;
import com.cfg.common.security.UserPrincipal;
import com.cfg.sync.dto.SyncBatchRequest;
import com.cfg.sync.service.SyncService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/sync")
@RequiredArgsConstructor
public class SyncController {

    private final SyncService syncService;

    @PostMapping("/batch")
    @PreAuthorize("hasAnyRole('WAITER','MANAGER','OWNER')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> syncBatch(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody SyncBatchRequest request) {
        List<Map<String, Object>> results = syncService.processBatch(principal, request);
        return ResponseEntity.ok(ApiResponse.ok(results));
    }
}
