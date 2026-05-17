package com.stockapi.service;

import com.stockapi.dto.PositionRequest;
import com.stockapi.dto.PositionResponse;
import com.stockapi.entity.Position;
import com.stockapi.entity.User;
import com.stockapi.repository.PositionRepository;
import com.stockapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PositionServiceTest {

    @Mock PositionRepository positionRepository;
    @Mock UserRepository userRepository;

    @InjectMocks PositionService positionService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("odgerel", "hashed");
        user.setId(1L);
        when(userRepository.findByUsername("odgerel")).thenReturn(Optional.of(user));
    }

    @Test
    void save_newPosition_creates() {
        PositionRequest req = new PositionRequest();
        req.setTicker("nvda");
        req.setQuantity(10);
        req.setAvgCost(850.0);
        req.setTargetPrice(1000.0);
        req.setStopPrice(750.0);

        when(positionRepository.findByUserIdAndTicker(1L, "NVDA")).thenReturn(Optional.empty());
        when(positionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        PositionResponse saved = positionService.save("odgerel", req);

        assertThat(saved.getTicker()).isEqualTo("NVDA");
        assertThat(saved.getQuantity()).isEqualTo(10);
        assertThat(saved.getAvgCost()).isEqualTo(850.0);
        assertThat(saved.getTargetPrice()).isEqualTo(1000.0);
        assertThat(saved.getStopPrice()).isEqualTo(750.0);
    }

    @Test
    void save_existingPosition_updates() {
        Position existing = new Position(user, "NVDA", 5, 800.0);
        when(positionRepository.findByUserIdAndTicker(1L, "NVDA")).thenReturn(Optional.of(existing));
        when(positionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        PositionRequest req = new PositionRequest();
        req.setTicker("NVDA");
        req.setQuantity(10);
        req.setAvgCost(850.0);

        PositionResponse saved = positionService.save("odgerel", req);

        assertThat(saved.getQuantity()).isEqualTo(10);
        assertThat(saved.getAvgCost()).isEqualTo(850.0);
    }

    @Test
    void delete_exists_removes() {
        Position pos = new Position(user, "NVDA", 10, 850.0);
        when(positionRepository.findByUserIdAndTicker(1L, "NVDA")).thenReturn(Optional.of(pos));

        positionService.delete("odgerel", "NVDA");

        verify(positionRepository).delete(pos);
    }

    @Test
    void calcPnl_returnsCorrectPercentage() {
        Position pos = new Position(user, "NVDA", 10, 800.0);
        when(positionRepository.findByUserId(1L)).thenReturn(List.of(pos));

        // Current price is 1000, bought at 800 → 25% gain
        Map<String, Double> pnl = positionService.calcPnl("odgerel", Map.of("NVDA", 1000.0));

        assertThat(pnl).containsKey("NVDA");
        assertThat(pnl.get("NVDA")).isCloseTo(25.0, within(0.01));
    }

    @Test
    void calcPnl_missingPrice_excluded() {
        Position pos = new Position(user, "NVDA", 10, 800.0);
        when(positionRepository.findByUserId(1L)).thenReturn(List.of(pos));

        Map<String, Double> pnl = positionService.calcPnl("odgerel", Map.of("AMD", 200.0));

        assertThat(pnl).doesNotContainKey("NVDA");
    }
}
