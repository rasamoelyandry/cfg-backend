package com.cfg.table.service;

import com.cfg.common.exception.BusinessException;
import com.cfg.common.exception.ResourceNotFoundException;
import com.cfg.common.exception.TenantAccessException;
import com.cfg.restaurant.repository.RestaurantRepository;
import com.cfg.table.domain.RestaurantTable;
import com.cfg.table.dto.CreateTableRequest;
import com.cfg.table.dto.TableResponse;
import com.cfg.table.dto.UpdateTableRequest;
import com.cfg.table.repository.TableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TableService {

    private final TableRepository tableRepository;
    private final RestaurantRepository restaurantRepository;

    @Transactional(readOnly = true)
    public List<TableResponse> getAll(UUID restaurantId) {
        return tableRepository.findAllByRestaurantIdAndIsActiveTrueOrderByNumber(restaurantId)
                .stream().map(TableResponse::from).collect(Collectors.toList());
    }

    @Transactional
    public TableResponse create(UUID restaurantId, CreateTableRequest req) {
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new ResourceNotFoundException("Restaurant", restaurantId);
        }
        if (tableRepository.existsByRestaurantIdAndNumber(restaurantId, req.getNumber())) {
            throw new BusinessException("Table number " + req.getNumber() + " already exists in this restaurant");
        }

        RestaurantTable table = RestaurantTable.builder()
                .restaurantId(restaurantId)
                .number(req.getNumber())
                .label(req.getLabel())
                .capacity(req.getCapacity())
                .isActive(true)
                .build();

        return TableResponse.from(tableRepository.save(table));
    }

    @Transactional
    public TableResponse update(UUID restaurantId, UUID tableId, UpdateTableRequest req) {
        RestaurantTable table = findInRestaurant(restaurantId, tableId);

        if (req.getLabel() != null)    table.setLabel(req.getLabel());
        if (req.getCapacity() != null) table.setCapacity(req.getCapacity());
        if (req.getIsActive() != null) table.setActive(req.getIsActive());

        return TableResponse.from(tableRepository.save(table));
    }

    @Transactional
    public void delete(UUID restaurantId, UUID tableId) {
        RestaurantTable table = findInRestaurant(restaurantId, tableId);
        // Soft delete
        table.setActive(false);
        tableRepository.save(table);
    }

    @Transactional
    public TableResponse release(UUID restaurantId, UUID tableId) {
        findInRestaurant(restaurantId, tableId);
        tableRepository.markFree(tableId);
        return TableResponse.from(findInRestaurant(restaurantId, tableId));
    }

    private RestaurantTable findInRestaurant(UUID restaurantId, UUID tableId) {
        RestaurantTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new ResourceNotFoundException("Table", tableId));
        if (!table.getRestaurantId().equals(restaurantId)) {
            throw new TenantAccessException();
        }
        return table;
    }
}
