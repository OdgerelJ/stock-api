package com.stockapi.controller;

import com.stockapi.service.WatchlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/watchlist")
@RequiredArgsConstructor
public class WatchlistController {

    private final WatchlistService watchlistService;

    @GetMapping
    public List<String> list(@AuthenticationPrincipal UserDetails user) {
        return watchlistService.getTickers(user.getUsername());
    }

    @PostMapping
    public ResponseEntity<Void> add(
        @AuthenticationPrincipal UserDetails user,
        @RequestBody Map<String, String> body
    ) {
        String ticker = body.get("ticker");
        if (ticker == null || ticker.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        watchlistService.addTicker(user.getUsername(), ticker);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{ticker}")
    public ResponseEntity<Void> remove(
        @AuthenticationPrincipal UserDetails user,
        @PathVariable String ticker
    ) {
        watchlistService.removeTicker(user.getUsername(), ticker);
        return ResponseEntity.noContent().build();
    }
}
