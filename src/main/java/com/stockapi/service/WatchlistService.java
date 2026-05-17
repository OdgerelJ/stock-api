package com.stockapi.service;

import com.stockapi.entity.User;
import com.stockapi.entity.WatchlistItem;
import com.stockapi.repository.UserRepository;
import com.stockapi.repository.WatchlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<String> getTickers(String username) {
        User user = findUser(username);
        return watchlistRepository.findByUserId(user.getId())
            .stream()
            .map(WatchlistItem::getTicker)
            .toList();
    }

    @Transactional
    public void addTicker(String username, String ticker) {
        User user = findUser(username);
        String upper = ticker.toUpperCase();
        if (watchlistRepository.existsByUserIdAndTicker(user.getId(), upper)) {
            return;
        }
        watchlistRepository.save(new WatchlistItem(user, upper));
    }

    @Transactional
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
