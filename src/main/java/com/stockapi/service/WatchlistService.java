package com.stockapi.service;

import com.stockapi.entity.User;
import com.stockapi.entity.WatchlistItem;
import com.stockapi.repository.UserRepository;
import com.stockapi.repository.WatchlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final UserRepository userRepository;

    public List<String> getTickers(String username) {
        User user = findUser(username);
        return watchlistRepository.findByUserId(user.getId())
            .stream()
            .map(WatchlistItem::getTicker)
            .toList();
    }

    public void addTicker(String username, String ticker) {
        User user = findUser(username);
        String upper = ticker.toUpperCase();
        if (watchlistRepository.existsByUserIdAndTicker(user.getId(), upper)) {
            return; // already in watchlist — silently ignore
        }
        watchlistRepository.save(new WatchlistItem(user, upper));
    }

    public void removeTicker(String username, String ticker) {
        User user = findUser(username);
        watchlistRepository
            .findByUserIdAndTicker(user.getId(), ticker.toUpperCase())
            .ifPresent(watchlistRepository::delete);
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalStateException("User not found"));
    }
}
