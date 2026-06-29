package com.cfg.restaurant.controller;

import com.cfg.common.dto.ApiResponse;
import com.cfg.common.security.UserPrincipal;
import com.cfg.restaurant.dto.CreateRestaurantRequest;
import com.cfg.restaurant.dto.RestaurantResponse;
import com.cfg.restaurant.dto.UpdateRestaurantRequest;
import com.cfg.restaurant.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<RestaurantResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(restaurantService.getAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','OWNER','MANAGER','WAITER','KITCHEN')")
    public ResponseEntity<ApiResponse<RestaurantResponse>> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(restaurantService.getById(id, principal)));
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<RestaurantResponse>> create(
            @Valid @RequestBody CreateRestaurantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(restaurantService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','OWNER')")
    public ResponseEntity<ApiResponse<RestaurantResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRestaurantRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(restaurantService.update(id, request, principal)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        restaurantService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Restaurant deleted"));
    }
}
