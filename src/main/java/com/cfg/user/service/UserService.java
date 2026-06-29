package com.cfg.user.service;

import com.cfg.common.exception.BusinessException;
import com.cfg.common.exception.ResourceNotFoundException;
import com.cfg.common.exception.TenantAccessException;
import com.cfg.common.security.UserPrincipal;
import com.cfg.restaurant.repository.RestaurantRepository;
import com.cfg.user.domain.Role;
import com.cfg.user.domain.User;
import com.cfg.user.dto.*;
import com.cfg.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserResponse> getAll(UUID restaurantId) {
        return userRepository.findAllByRestaurantIdAndIsActiveTrue(restaurantId)
                .stream().map(UserResponse::from).collect(Collectors.toList());
    }

    @Transactional
    public UserResponse create(UUID restaurantId, CreateUserRequest req, UserPrincipal creator) {
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new ResourceNotFoundException("Restaurant", restaurantId);
        }

        // Un OWNER ne peut créer que des rôles inférieurs au sien
        validateRoleCreation(creator, req.getRole());

        if (req.getEmail() == null && req.getPhone() == null) {
            throw new BusinessException("Email or phone is required");
        }
        if (req.getEmail() != null && userRepository.existsByEmail(req.getEmail())) {
            throw new BusinessException("Email already in use");
        }
        if (req.getPhone() != null && userRepository.existsByPhone(req.getPhone())) {
            throw new BusinessException("Phone already in use");
        }

        User user = User.builder()
                .restaurantId(restaurantId)
                .email(req.getEmail())
                .phone(req.getPhone())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .role(req.getRole())
                .isActive(true)
                .build();

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public UserResponse update(UUID restaurantId, UUID userId, UpdateUserRequest req) {
        User user = findInRestaurant(restaurantId, userId);

        if (req.getFirstName() != null) user.setFirstName(req.getFirstName());
        if (req.getLastName() != null)  user.setLastName(req.getLastName());
        if (req.getIsActive() != null)  user.setActive(req.getIsActive());

        if (req.getEmail() != null && !req.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(req.getEmail())) {
                throw new BusinessException("Email already in use");
            }
            user.setEmail(req.getEmail());
        }
        if (req.getPhone() != null && !req.getPhone().equals(user.getPhone())) {
            if (userRepository.existsByPhone(req.getPhone())) {
                throw new BusinessException("Phone already in use");
            }
            user.setPhone(req.getPhone());
        }
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        }

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateRole(UUID restaurantId, UUID userId, UpdateRoleRequest req,
                                   UserPrincipal actor) {
        User user = findInRestaurant(restaurantId, userId);
        validateRoleCreation(actor, req.getRole());
        user.setRole(req.getRole());
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public void delete(UUID restaurantId, UUID userId) {
        User user = findInRestaurant(restaurantId, userId);
        user.setActive(false);
        userRepository.save(user);
    }

    // ─── Helpers ─────────────────────────────────────────────────

    private User findInRestaurant(UUID restaurantId, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        if (!restaurantId.equals(user.getRestaurantId())) {
            throw new TenantAccessException();
        }
        return user;
    }

    private void validateRoleCreation(UserPrincipal creator, Role targetRole) {
        // SUPER_ADMIN peut tout créer
        if (creator.getRole().equals(Role.SUPER_ADMIN.name())) return;

        // OWNER peut créer MANAGER, WAITER, KITCHEN mais pas OWNER ni SUPER_ADMIN
        if (creator.getRole().equals(Role.OWNER.name())) {
            if (targetRole == Role.OWNER || targetRole == Role.SUPER_ADMIN) {
                throw new BusinessException("You cannot assign this role");
            }
            return;
        }

        // MANAGER peut créer WAITER et KITCHEN
        if (creator.getRole().equals(Role.MANAGER.name())) {
            if (targetRole == Role.WAITER || targetRole == Role.KITCHEN) return;
            throw new BusinessException("You cannot assign this role");
        }

        throw new BusinessException("You are not allowed to create users");
    }
}
