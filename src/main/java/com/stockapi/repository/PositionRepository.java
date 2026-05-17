package com.stockapi.repository;

import com.stockapi.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PositionRepository extends JpaRepository<Position, Long> {
    List<Position> findByUserId(Long userId);
    Optional<Position> findByUserIdAndTicker(Long userId, String ticker);
}
