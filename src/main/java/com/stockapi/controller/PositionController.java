package com.stockapi.controller;

import com.stockapi.dto.PositionRequest;
import com.stockapi.dto.PositionResponse;
import com.stockapi.service.PositionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/positions")
@RequiredArgsConstructor
public class PositionController {

    private final PositionService positionService;

    @GetMapping
    public List<PositionResponse> list(@AuthenticationPrincipal UserDetails user) {
        return positionService.getPositions(user.getUsername());
    }

    @PutMapping
    public PositionResponse save(
        @AuthenticationPrincipal UserDetails user,
        @Valid @RequestBody PositionRequest req
    ) {
        return positionService.save(user.getUsername(), req);
    }

    @DeleteMapping("/{ticker}")
    public ResponseEntity<Void> delete(
        @AuthenticationPrincipal UserDetails user,
        @PathVariable String ticker
    ) {
        positionService.delete(user.getUsername(), ticker);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/pnl")
    public Map<String, Double> pnl(
        @AuthenticationPrincipal UserDetails user,
        @RequestBody Map<String, Double> currentPrices
    ) {
        return positionService.calcPnl(user.getUsername(), currentPrices);
    }
}
