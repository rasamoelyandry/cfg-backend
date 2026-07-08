package com.cfg.user.controller;

import com.cfg.common.dto.ApiResponse;
import com.cfg.common.security.UserPrincipal;
import com.cfg.user.dto.CreateUserRequest;
import com.cfg.user.dto.UpdateUserRequest;
import com.cfg.user.dto.UserResponse;
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

/**
 * Gestion des comptes SUPER_ADMIN (administrateurs plateforme, non rattaches a un restaurant).
 * Distinct de UserController, qui gere le personnel d'un restaurant precis.
 */
@RestController
@RequestMapping("/api/v1/platform-admins")
@RequiredArgsConstructor
public class PlatformAdminController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(userService.getAllPlatformAdmins()));
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> create(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(userService.createPlatformAdmin(request)));
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> update(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(userService.updatePlatformAdmin(userId, request)));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID userId,
            @AuthenticationPrincipal UserPrincipal principal) {
        userService.deletePlatformAdmin(userId, principal);
        return ResponseEntity.ok(ApiResponse.ok("Administrateur désactivé"));
    }
}
