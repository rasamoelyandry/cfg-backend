package com.cfg.menu.dto;

import com.cfg.menu.domain.MenuCategory;
import com.cfg.menu.domain.MenuItem;
import com.cfg.menu.domain.MenuItemModifier;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
public class MenuResponse {
    private List<CategoryDto> categories;

    @Data
    @Builder
    public static class CategoryDto {
        private UUID id;
        private String name;
        private String description;
        private int sortOrder;
        @JsonProperty("isActive")
        private boolean isActive;
        private List<MenuItemDto> items;

        public static CategoryDto from(MenuCategory c, List<MenuItem> items) {
            return CategoryDto.builder()
                    .id(c.getId())
                    .name(c.getName())
                    .description(c.getDescription())
                    .sortOrder(c.getSortOrder())
                    .isActive(c.isActive())
                    .items(items.stream().map(MenuItemDto::from).collect(Collectors.toList()))
                    .build();
        }
    }

    @Data
    @Builder
    public static class MenuItemDto {
        private UUID id;
        private UUID categoryId;
        private String name;
        private String description;
        private BigDecimal price;
        private String imageUrl;
        @JsonProperty("isAvailable")
        private boolean isAvailable;
        private int sortOrder;
        private boolean trackStock;
        private int stockQuantity;
        private List<ModifierDto> modifiers;

        public static MenuItemDto from(MenuItem i) {
            return MenuItemDto.builder()
                    .id(i.getId())
                    .categoryId(i.getCategoryId())
                    .name(i.getName())
                    .description(i.getDescription())
                    .price(i.getPrice())
                    .imageUrl(i.getImageUrl())
                    .isAvailable(i.isAvailable())
                    .sortOrder(i.getSortOrder())
                    .trackStock(i.isTrackStock())
                    .stockQuantity(i.getStockQuantity())
                    .modifiers(i.getModifiers().stream().map(ModifierDto::from).collect(Collectors.toList()))
                    .build();
        }
    }

    @Data
    @Builder
    public static class ModifierDto {
        private UUID id;
        private String name;
        private BigDecimal priceDelta;
        @JsonProperty("isDefault")
        private boolean isDefault;

        public static ModifierDto from(MenuItemModifier m) {
            return ModifierDto.builder()
                    .id(m.getId())
                    .name(m.getName())
                    .priceDelta(m.getPriceDelta())
                    .isDefault(m.isDefault())
                    .build();
        }
    }
}
