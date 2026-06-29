package com.cfg.sync.repository;

import com.cfg.sync.domain.SyncEvent;
import com.cfg.sync.domain.SyncStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SyncEventRepository extends JpaRepository<SyncEvent, UUID> {
    Optional<SyncEvent> findByClientUuid(UUID clientUuid);
    boolean existsByClientUuid(UUID clientUuid);
    long countByUserIdAndStatus(UUID userId, SyncStatus status);
}
