package com.stockapi.controller;

import com.stockapi.dto.WatchlistRequest;
import com.stockapi.service.WatchlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        @Valid @RequestBody WatchlistRequest req
    ) {
        watchlistService.addTicker(user.getUsername(), req.getTicker());
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
