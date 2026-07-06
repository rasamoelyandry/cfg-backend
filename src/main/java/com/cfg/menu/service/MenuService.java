package com.cfg.menu.service;

import com.cfg.common.exception.BusinessException;
import com.cfg.common.exception.ResourceNotFoundException;
import com.cfg.common.exception.TenantAccessException;
import com.cfg.menu.domain.MenuCategory;
import com.cfg.menu.domain.MenuItem;
import com.cfg.menu.domain.MenuItemModifier;
import com.cfg.menu.dto.*;
import com.cfg.menu.repository.MenuCategoryRepository;
import com.cfg.menu.repository.MenuItemRepository;
import com.cfg.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuCategoryRepository categoryRepository;
    private final MenuItemRepository itemRepository;
    private final RestaurantRepository restaurantRepository;

    // ─── Menu complet ────────────────────────────────────────────

    @Transactional(readOnly = true)
    public MenuResponse getFullMenu(UUID restaurantId) {
        List<MenuCategory> categories =
                categoryRepository.findAllByRestaurantIdAndIsActiveTrueOrderBySortOrder(restaurantId);

        List<MenuItem> allItems =
                itemRepository.findAllByRestaurantIdOrderByCategoryIdAscSortOrderAsc(restaurantId);

        // Grouper les items par catégorie
        Map<UUID, List<MenuItem>> byCategory = allItems.stream()
                .collect(Collectors.groupingBy(MenuItem::getCategoryId));

        List<MenuResponse.CategoryDto> categoryDtos = categories.stream()
                .map(cat -> MenuResponse.CategoryDto.from(
                        cat,
                        byCategory.getOrDefault(cat.getId(), List.of())))
                .collect(Collectors.toList());

        return MenuResponse.builder().categories(categoryDtos).build();
    }

    // ─── Catégories ──────────────────────────────────────────────

    @Transactional
    public MenuResponse.CategoryDto createCategory(UUID restaurantId, CreateCategoryRequest req) {
        requireRestaurant(restaurantId);
        MenuCategory category = MenuCategory.builder()
                .restaurantId(restaurantId)
                .name(req.getName())
                .description(req.getDescription())
                .sortOrder(req.getSortOrder())
                .isActive(true)
                .build();
        return MenuResponse.CategoryDto.from(categoryRepository.save(category), List.of());
    }

    @Transactional
    public MenuResponse.CategoryDto updateCategory(UUID restaurantId, UUID categoryId,
                                                    UpdateCategoryRequest req) {
        MenuCategory category = findCategory(restaurantId, categoryId);

        if (req.getName() != null)      category.setName(req.getName());
        if (req.getDescription() != null) category.setDescription(req.getDescription());
        if (req.getSortOrder() != null) category.setSortOrder(req.getSortOrder());
        if (req.getIsActive() != null)  category.setActive(req.getIsActive());

        List<MenuItem> items =
                itemRepository.findAllByCategoryIdAndIsAvailableTrueOrderBySortOrder(categoryId);
        return MenuResponse.CategoryDto.from(categoryRepository.save(category), items);
    }

    @Transactional
    public void deleteCategory(UUID restaurantId, UUID categoryId) {
        MenuCategory category = findCategory(restaurantId, categoryId);
        // Soft delete: désactiver items aussi
        List<MenuItem> items = itemRepository.findAllByCategoryIdAndIsAvailableTrueOrderBySortOrder(categoryId);
        items.forEach(i -> i.setAvailable(false));
        itemRepository.saveAll(items);
        category.setActive(false);
        categoryRepository.save(category);
    }

    // ─── Items ───────────────────────────────────────────────────

    @Transactional
    public MenuResponse.MenuItemDto createItem(UUID restaurantId, CreateMenuItemRequest req) {
        findCategory(restaurantId, req.getCategoryId());

        List<MenuItemModifier> modifiers = new ArrayList<>();
        if (req.getModifiers() != null) {
            modifiers = req.getModifiers().stream()
                    .map(m -> MenuItemModifier.builder()
                            .name(m.getName())
                            .priceDelta(m.getPriceDelta())
                            .isDefault(m.isDefault())
                            .build())
                    .collect(Collectors.toList());
        }

        MenuItem item = MenuItem.builder()
                .restaurantId(restaurantId)
                .categoryId(req.getCategoryId())
                .name(req.getName())
                .description(req.getDescription())
                .price(req.getPrice())
                .imageUrl(req.getImageUrl())
                .sortOrder(req.getSortOrder())
                .isAvailable(!req.isTrackStock() || req.getStockQuantity() > 0)
                .trackStock(req.isTrackStock())
                .stockQuantity(req.getStockQuantity())
                .modifiers(modifiers)
                .build();

        return MenuResponse.MenuItemDto.from(itemRepository.save(item));
    }

    @Transactional
    public MenuResponse.MenuItemDto updateItem(UUID restaurantId, UUID itemId,
                                                UpdateMenuItemRequest req) {
        MenuItem item = findItem(restaurantId, itemId);

        if (req.getCategoryId() != null) {
            findCategory(restaurantId, req.getCategoryId()); // validate belongs to restaurant
            item.setCategoryId(req.getCategoryId());
        }
        if (req.getName() != null)        item.setName(req.getName());
        if (req.getDescription() != null) item.setDescription(req.getDescription());
        if (req.getPrice() != null)       item.setPrice(req.getPrice());
        if (req.getImageUrl() != null)    item.setImageUrl(req.getImageUrl());
        if (req.getSortOrder() != null)   item.setSortOrder(req.getSortOrder());
        if (req.getTrackStock() != null)  item.setTrackStock(req.getTrackStock());
        if (req.getStockQuantity() != null) item.setStockQuantity(req.getStockQuantity());

        if (item.isTrackStock()) {
            // Dispo derivee automatiquement du stock quand le suivi est actif
            item.setAvailable(item.getStockQuantity() > 0);
        } else if (req.getIsAvailable() != null) {
            item.setAvailable(req.getIsAvailable());
        }

        return MenuResponse.MenuItemDto.from(itemRepository.save(item));
    }

    @Transactional
    public MenuResponse.MenuItemDto setAvailability(UUID restaurantId, UUID itemId, boolean available) {
        MenuItem item = findItem(restaurantId, itemId);
        if (item.isTrackStock()) {
            throw new BusinessException("Cet article suit son stock : la disponibilité est automatique, réapprovisionnez-le plutôt");
        }
        item.setAvailable(available);
        return MenuResponse.MenuItemDto.from(itemRepository.save(item));
    }

    @Transactional
    public MenuResponse.MenuItemDto restockItem(UUID restaurantId, UUID itemId, int quantity) {
        if (quantity <= 0) {
            throw new BusinessException("La quantité à ajouter doit être positive");
        }
        MenuItem item = findItem(restaurantId, itemId);
        if (!item.isTrackStock()) {
            throw new BusinessException("Cet article ne suit pas son stock — activez le suivi de stock d'abord");
        }
        itemRepository.addStock(itemId, quantity);
        return MenuResponse.MenuItemDto.from(findItem(restaurantId, itemId));
    }

    @Transactional
    public void deleteItem(UUID restaurantId, UUID itemId) {
        MenuItem item = findItem(restaurantId, itemId);
        item.setAvailable(false);
        itemRepository.save(item);
    }

    // ─── Duplication ─────────────────────────────────────────────

    @Transactional
    public MenuResponse duplicateMenu(UUID sourceId, UUID targetId) {
        if (sourceId.equals(targetId)) {
            throw new BusinessException("Source et destination identiques");
        }
        requireRestaurant(sourceId);
        requireRestaurant(targetId);

        // Soft-delete existing categories + items du restaurant cible
        List<MenuCategory> existingCats =
                categoryRepository.findAllByRestaurantIdAndIsActiveTrueOrderBySortOrder(targetId);
        for (MenuCategory cat : existingCats) {
            List<MenuItem> items =
                    itemRepository.findAllByCategoryIdAndIsAvailableTrueOrderBySortOrder(cat.getId());
            items.forEach(i -> i.setAvailable(false));
            itemRepository.saveAll(items);
            cat.setActive(false);
        }
        categoryRepository.saveAll(existingCats);

        // Charger les données sources
        List<MenuCategory> sourceCats =
                categoryRepository.findAllByRestaurantIdAndIsActiveTrueOrderBySortOrder(sourceId);
        List<MenuItem> sourceItems =
                itemRepository.findAllByRestaurantIdAndIsAvailableTrueOrderByCategoryIdAscSortOrderAsc(sourceId);

        Map<UUID, List<MenuItem>> byCategory = sourceItems.stream()
                .collect(Collectors.groupingBy(MenuItem::getCategoryId));

        // Copier catégories + items vers la cible
        for (MenuCategory src : sourceCats) {
            MenuCategory newCat = categoryRepository.save(
                    MenuCategory.builder()
                            .restaurantId(targetId)
                            .name(src.getName())
                            .description(src.getDescription())
                            .sortOrder(src.getSortOrder())
                            .isActive(true)
                            .build()
            );

            List<MenuItem> catItems = byCategory.getOrDefault(src.getId(), List.of());
            for (MenuItem srcItem : catItems) {
                List<MenuItemModifier> newMods = srcItem.getModifiers().stream()
                        .map(m -> MenuItemModifier.builder()
                                .name(m.getName())
                                .priceDelta(m.getPriceDelta())
                                .isDefault(m.isDefault())
                                .build())
                        .collect(Collectors.toList());

                itemRepository.save(
                        MenuItem.builder()
                                .restaurantId(targetId)
                                .categoryId(newCat.getId())
                                .name(srcItem.getName())
                                .description(srcItem.getDescription())
                                .price(srcItem.getPrice())
                                .imageUrl(srcItem.getImageUrl())
                                .sortOrder(srcItem.getSortOrder())
                                .isAvailable(true)
                                .modifiers(newMods)
                                .build()
                );
            }
        }

        return getFullMenu(targetId);
    }

    // ─── Helpers ─────────────────────────────────────────────────

    private void requireRestaurant(UUID restaurantId) {
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new ResourceNotFoundException("Restaurant", restaurantId);
        }
    }

    private MenuCategory findCategory(UUID restaurantId, UUID categoryId) {
        MenuCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("MenuCategory", categoryId));
        if (!category.getRestaurantId().equals(restaurantId)) {
            throw new TenantAccessException("Category does not belong to this restaurant");
        }
        return category;
    }

    private MenuItem findItem(UUID restaurantId, UUID itemId) {
        MenuItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", itemId));
        if (!item.getRestaurantId().equals(restaurantId)) {
            throw new TenantAccessException("Item does not belong to this restaurant");
        }
        return item;
    }
}
