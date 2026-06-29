package com.cfg.restaurant.service;

import com.cfg.common.exception.BusinessException;
import com.cfg.common.exception.ResourceNotFoundException;
import com.cfg.common.exception.TenantAccessException;
import com.cfg.common.security.UserPrincipal;
import com.cfg.common.util.SlugUtils;
import com.cfg.restaurant.domain.Restaurant;
import com.cfg.restaurant.dto.CreateRestaurantRequest;
import com.cfg.restaurant.dto.RestaurantResponse;
import com.cfg.restaurant.dto.UpdateRestaurantRequest;
import com.cfg.restaurant.repository.RestaurantRepository;
import com.cfg.user.domain.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    @Transactional(readOnly = true)
    public List<RestaurantResponse> getAll() {
        return restaurantRepository.findAll().stream()
                .map(RestaurantResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RestaurantResponse getById(UUID id, UserPrincipal principal) {
        Restaurant restaurant = findById(id);
        checkAccess(principal, id);
        return RestaurantResponse.from(restaurant);
    }

    @Transactional
    public RestaurantResponse create(CreateRestaurantRequest req) {
        String slug = buildUniqueSlug(req.getSlug() != null ? req.getSlug() : req.getName());

        Restaurant restaurant = Restaurant.builder()
                .name(req.getName())
                .slug(slug)
                .address(req.getAddress())
                .phone(req.getPhone())
                .email(req.getEmail())
                .currency(req.getCurrency() != null ? req.getCurrency() : "MGA")
                .timezone(req.getTimezone() != null ? req.getTimezone() : "Indian/Antananarivo")
                .isActive(true)
                .build();

        return RestaurantResponse.from(restaurantRepository.save(restaurant));
    }

    @Transactional
    public RestaurantResponse update(UUID id, UpdateRestaurantRequest req, UserPrincipal principal) {
        Restaurant restaurant = findById(id);
        checkAccess(principal, id);

        if (req.getName() != null)     restaurant.setName(req.getName());
        if (req.getAddress() != null)  restaurant.setAddress(req.getAddress());
        if (req.getPhone() != null)    restaurant.setPhone(req.getPhone());
        if (req.getEmail() != null)    restaurant.setEmail(req.getEmail());
        if (req.getCurrency() != null) restaurant.setCurrency(req.getCurrency());
        if (req.getTimezone() != null) restaurant.setTimezone(req.getTimezone());
        if (req.getIsActive() != null) restaurant.setActive(req.getIsActive());

        return RestaurantResponse.from(restaurantRepository.save(restaurant));
    }

    @Transactional
    public void delete(UUID id) {
        if (!restaurantRepository.existsById(id)) {
            throw new ResourceNotFoundException("Restaurant", id);
        }
        restaurantRepository.deleteById(id);
    }

    // --- helpers ---

    private Restaurant findById(UUID id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", id));
    }

    private void checkAccess(UserPrincipal principal, UUID restaurantId) {
        if (principal.getRole().equals(Role.SUPER_ADMIN.name())) return;
        if (!restaurantId.equals(principal.getRestaurantId())) {
            throw new TenantAccessException();
        }
    }

    private String buildUniqueSlug(String base) {
        String candidate = SlugUtils.toSlug(base);
        if (!restaurantRepository.existsBySlug(candidate)) return candidate;

        for (int i = 2; i <= 99; i++) {
            String attempt = candidate + "-" + i;
            if (!restaurantRepository.existsBySlug(attempt)) return attempt;
        }
        throw new BusinessException("Could not generate unique slug for: " + base);
    }
}
