package com.stockapi.service;

import com.stockapi.dto.PositionRequest;
import com.stockapi.entity.Position;
import com.stockapi.entity.User;
import com.stockapi.repository.PositionRepository;
import com.stockapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PositionService {

    private final PositionRepository positionRepository;
    private final UserRepository userRepository;

    public List<Position> getPositions(String username) {
        return positionRepository.findByUserId(findUser(username).getId());
    }

    public Position save(String username, PositionRequest req) {
        User user = findUser(username);
        String ticker = req.getTicker().toUpperCase();

        Position pos = positionRepository
            .findByUserIdAndTicker(user.getId(), ticker)
            .orElse(new Position(user, ticker, 0, 0));

        pos.setQuantity(req.getQuantity());
        pos.setAvgCost(req.getAvgCost());
        pos.setTargetPrice(req.getTargetPrice());
        pos.setStopPrice(req.getStopPrice());
        return positionRepository.save(pos);
    }

    public void delete(String username, String ticker) {
        User user = findUser(username);
        positionRepository
            .findByUserIdAndTicker(user.getId(), ticker.toUpperCase())
            .ifPresent(positionRepository::delete);
    }

    // Returns a map of ticker -> P&L percentage given current prices
    public Map<String, Double> calcPnl(String username, Map<String, Double> currentPrices) {
        return getPositions(username).stream()
            .filter(p -> currentPrices.containsKey(p.getTicker()))
            .collect(Collectors.toMap(
                Position::getTicker,
                p -> ((currentPrices.get(p.getTicker()) - p.getAvgCost()) / p.getAvgCost()) * 100
            ));
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalStateException("User not found"));
    }
}
