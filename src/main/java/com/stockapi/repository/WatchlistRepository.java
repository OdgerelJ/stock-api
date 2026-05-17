package com.stockapi.repository;

import com.stockapi.entity.WatchlistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WatchlistRepository extends JpaRepository<WatchlistItem, Long> {
    List<WatchlistItem> findByUserId(Long userId);
    Optional<WatchlistItem> findByUserIdAndTicker(Long userId, String ticker);
    boolean existsByUserIdAndTicker(Long userId, String ticker);
}
