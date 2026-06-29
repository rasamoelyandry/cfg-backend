package com.cfg.user.controller;

import com.cfg.common.dto.ApiResponse;
import com.cfg.common.security.UserPrincipal;
import com.cfg.user.dto.LoginRequest;
import com.cfg.user.dto.LoginResponse;
import com.cfg.user.dto.UserResponse;
import com.cfg.user.repository.UserRepository;
import com.cfg.user.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.login(request)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(ApiResponse.ok(authService.refresh(body.get("refreshToken"))));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal UserPrincipal principal) {
        authService.logout(principal.getId().toString());
        return ResponseEntity.ok(ApiResponse.ok("Logged out"));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> me(@AuthenticationPrincipal UserPrincipal principal) {
        var user = userRepository.findById(principal.getId())
                .orElseThrow();
        return ResponseEntity.ok(ApiResponse.ok(UserResponse.from(user)));
    }
}
