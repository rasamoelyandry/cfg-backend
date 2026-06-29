package com.cfg.menu.controller;

import com.cfg.common.dto.ApiResponse;
import com.cfg.menu.dto.*;
import com.cfg.menu.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/restaurants/{restaurantId}")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    // ─── Menu complet ─────────────────────────────────────────────

    @GetMapping("/menu")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','OWNER','MANAGER','WAITER','KITCHEN')")
    public ResponseEntity<ApiResponse<MenuResponse>> getMenu(@PathVariable UUID restaurantId) {
        return ResponseEntity.ok(ApiResponse.ok(menuService.getFullMenu(restaurantId)));
    }

    // ─── Catégories ──────────────────────────────────────────────

    @PostMapping("/categories")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','OWNER','MANAGER')")
    public ResponseEntity<ApiResponse<MenuResponse.CategoryDto>> createCategory(
            @PathVariable UUID restaurantId,
            @Valid @RequestBody CreateCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(menuService.createCategory(restaurantId, request)));
    }

    @PutMapping("/categories/{categoryId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','OWNER','MANAGER')")
    public ResponseEntity<ApiResponse<MenuResponse.CategoryDto>> updateCategory(
            @PathVariable UUID restaurantId,
            @PathVariable UUID categoryId,
            @Valid @RequestBody UpdateCategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                menuService.updateCategory(restaurantId, categoryId, request)));
    }

    @DeleteMapping("/categories/{categoryId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','OWNER','MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @PathVariable UUID restaurantId,
            @PathVariable UUID categoryId) {
        menuService.deleteCategory(restaurantId, categoryId);
        return ResponseEntity.ok(ApiResponse.ok("Category deactivated"));
    }

    // ─── Items ───────────────────────────────────────────────────

    @PostMapping("/items")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','OWNER','MANAGER')")
    public ResponseEntity<ApiResponse<MenuResponse.MenuItemDto>> createItem(
            @PathVariable UUID restaurantId,
            @Valid @RequestBody CreateMenuItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(menuService.createItem(restaurantId, request)));
    }

    @PutMapping("/items/{itemId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','OWNER','MANAGER')")
    public ResponseEntity<ApiResponse<MenuResponse.MenuItemDto>> updateItem(
            @PathVariable UUID restaurantId,
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateMenuItemRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                menuService.updateItem(restaurantId, itemId, request)));
    }

    @PatchMapping("/items/{itemId}/availability")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','OWNER','MANAGER')")
    public ResponseEntity<ApiResponse<MenuResponse.MenuItemDto>> setAvailability(
            @PathVariable UUID restaurantId,
            @PathVariable UUID itemId,
            @RequestBody Map<String, Boolean> body) {
        boolean available = Boolean.TRUE.equals(body.get("isAvailable"));
        return ResponseEntity.ok(ApiResponse.ok(
                menuService.setAvailability(restaurantId, itemId, available)));
    }

    @DeleteMapping("/items/{itemId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','OWNER','MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteItem(
            @PathVariable UUID restaurantId,
            @PathVariable UUID itemId) {
        menuService.deleteItem(restaurantId, itemId);
        return ResponseEntity.ok(ApiResponse.ok("Item deactivated"));
    }
}
