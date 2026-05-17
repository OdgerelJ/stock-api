package com.stockapi.service;

import com.stockapi.dto.PositionRequest;
import com.stockapi.dto.PositionResponse;
import com.stockapi.entity.Position;
import com.stockapi.entity.User;
import com.stockapi.repository.PositionRepository;
import com.stockapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PositionService {

    private final PositionRepository positionRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<PositionResponse> getPositions(String username) {
        return positionRepository.findByUserId(findUser(username).getId())
            .stream().map(this::toResponse).toList();
    }

    @Transactional
    public PositionResponse save(String username, PositionRequest req) {
        User user = findUser(username);
        String ticker = req.getTicker().toUpperCase();

        Position pos = positionRepository
            .findByUserIdAndTicker(user.getId(), ticker)
            .orElse(new Position(user, ticker, 0, 0));

        pos.setQuantity(req.getQuantity());
        pos.setAvgCost(req.getAvgCost());
        pos.setTargetPrice(req.getTargetPrice());
        pos.setStopPrice(req.getStopPrice());
        return toResponse(positionRepository.save(pos));
    }

    @Transactional
    public void delete(String username, String ticker) {
        User user = findUser(username);
        positionRepository
            .findByUserIdAndTicker(user.getId(), ticker.toUpperCase())
            .ifPresent(positionRepository::delete);
    }

    @Transactional(readOnly = true)
    public Map<String, Double> calcPnl(String username, Map<String, Double> currentPrices) {
        return getPositions(username).stream()
            .filter(p -> currentPrices.containsKey(p.getTicker()))
            .collect(Collectors.toMap(
                PositionResponse::getTicker,
                p -> ((currentPrices.get(p.getTicker()) - p.getAvgCost()) / p.getAvgCost()) * 100
            ));
    }

    private PositionResponse toResponse(Position p) {
        return new PositionResponse(
            p.getId(), p.getTicker(), p.getQuantity(),
            p.getAvgCost(), p.getTargetPrice(), p.getStopPrice()
        );
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalStateException("User not found"));
    }
}
