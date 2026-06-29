package com.cfg.table.controller;

import com.cfg.common.dto.ApiResponse;
import com.cfg.table.dto.CreateTableRequest;
import com.cfg.table.dto.TableResponse;
import com.cfg.table.dto.UpdateTableRequest;
import com.cfg.table.service.TableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/restaurants/{restaurantId}/tables")
@RequiredArgsConstructor
public class TableController {

    private final TableService tableService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','OWNER','MANAGER','WAITER','KITCHEN')")
    public ResponseEntity<ApiResponse<List<TableResponse>>> getAll(
            @PathVariable UUID restaurantId) {
        return ResponseEntity.ok(ApiResponse.ok(tableService.getAll(restaurantId)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','OWNER','MANAGER')")
    public ResponseEntity<ApiResponse<TableResponse>> create(
            @PathVariable UUID restaurantId,
            @Valid @RequestBody CreateTableRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(tableService.create(restaurantId, request)));
    }

    @PutMapping("/{tableId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','OWNER','MANAGER')")
    public ResponseEntity<ApiResponse<TableResponse>> update(
            @PathVariable UUID restaurantId,
            @PathVariable UUID tableId,
            @Valid @RequestBody UpdateTableRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(tableService.update(restaurantId, tableId, request)));
    }

    @DeleteMapping("/{tableId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','OWNER','MANAGER')")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID restaurantId,
            @PathVariable UUID tableId) {
        tableService.delete(restaurantId, tableId);
        return ResponseEntity.ok(ApiResponse.ok("Table deleted"));
    }
}
