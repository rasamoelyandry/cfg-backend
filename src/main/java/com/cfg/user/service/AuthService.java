package com.cfg.user.service;

import com.cfg.common.exception.BusinessException;
import com.cfg.common.exception.InvalidCredentialsException;
import com.cfg.common.security.JwtService;
import com.cfg.user.domain.User;
import com.cfg.user.dto.LoginRequest;
import com.cfg.user.dto.LoginResponse;
import com.cfg.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final StringRedisTemplate redis;

    private static final String REFRESH_PREFIX = "refresh:";
    private static final long REFRESH_TTL_DAYS = 7;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = findUserByCredentials(request);

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }
        if (!user.isActive()) {
            throw new InvalidCredentialsException("Account is disabled");
        }

        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(
                user.getId(), user.getRole().name(), user.getRestaurantId());
        String refreshToken = jwtService.generateRefreshToken(user.getId());

        try {
            redis.opsForValue().set(
                    REFRESH_PREFIX + user.getId(),
                    refreshToken,
                    REFRESH_TTL_DAYS, TimeUnit.DAYS
            );
        } catch (Exception e) {
            log.warn("Redis unavailable — refresh token not persisted (restart will require re-login): {}", e.getMessage());
        }

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .user(LoginResponse.UserDto.builder()
                        .id(user.getId())
                        .restaurantId(user.getRestaurantId())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .role(user.getRole().name())
                        .build())
                .build();
    }

    public LoginResponse refresh(String refreshToken) {
        if (!jwtService.isTokenValid(refreshToken)) {
            throw new InvalidCredentialsException("Invalid refresh token");
        }
        var userId = jwtService.extractUserId(refreshToken);
        try {
            String stored = redis.opsForValue().get(REFRESH_PREFIX + userId);
            if (stored == null || !stored.equals(refreshToken)) {
                throw new InvalidCredentialsException("Refresh token expired or revoked");
            }
        } catch (InvalidCredentialsException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Redis unavailable — skipping refresh token validation: {}", e.getMessage());
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));
        String newAccess = jwtService.generateAccessToken(
                user.getId(), user.getRole().name(), user.getRestaurantId());
        return LoginResponse.builder()
                .accessToken(newAccess)
                .tokenType("Bearer")
                .build();
    }

    public void logout(String userId) {
        try {
            redis.delete(REFRESH_PREFIX + userId);
        } catch (Exception e) {
            log.warn("Redis unavailable — token not invalidated server-side: {}", e.getMessage());
        }
    }

    private User findUserByCredentials(LoginRequest req) {
        if (req.getEmail() != null && !req.getEmail().isBlank()) {
            return userRepository.findByEmail(req.getEmail())
                    .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));
        }
        if (req.getPhone() != null && !req.getPhone().isBlank()) {
            return userRepository.findByPhone(req.getPhone())
                    .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));
        }
        throw new BusinessException("Email or phone is required");
    }
}
