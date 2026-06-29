package com.cfg.user.controller;

import com.cfg.common.dto.ApiResponse;
import com.cfg.common.security.UserPrincipal;
import com.cfg.user.dto.*;
import com.cfg.user.service.UserService;
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
@RequestMapping("/api/v1/restaurants/{restaurantId}/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','OWNER','MANAGER')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAll(
            @PathVariable UUID restaurantId) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getAll(restaurantId)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','OWNER','MANAGER')")
    public ResponseEntity<ApiResponse<UserResponse>> create(
            @PathVariable UUID restaurantId,
            @Valid @RequestBody CreateUserRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(userService.create(restaurantId, request, principal)));
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','OWNER','MANAGER')")
    public ResponseEntity<ApiResponse<UserResponse>> update(
            @PathVariable UUID restaurantId,
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(userService.update(restaurantId, userId, request)));
    }

    @PatchMapping("/{userId}/role")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','OWNER')")
    public ResponseEntity<ApiResponse<UserResponse>> updateRole(
            @PathVariable UUID restaurantId,
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateRoleRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                userService.updateRole(restaurantId, userId, request, principal)));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','OWNER')")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID restaurantId,
            @PathVariable UUID userId) {
        userService.delete(restaurantId, userId);
        return ResponseEntity.ok(ApiResponse.ok("User deactivated"));
    }
}
