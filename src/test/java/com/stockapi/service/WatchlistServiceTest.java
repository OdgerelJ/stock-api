package com.stockapi.service;

import com.stockapi.entity.User;
import com.stockapi.entity.WatchlistItem;
import com.stockapi.repository.UserRepository;
import com.stockapi.repository.WatchlistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WatchlistServiceTest {

    @Mock WatchlistRepository watchlistRepository;
    @Mock UserRepository userRepository;

    @InjectMocks WatchlistService watchlistService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("odgerel", "hashed");
        user.setId(1L);
        when(userRepository.findByUsername("odgerel")).thenReturn(Optional.of(user));
    }

    @Test
    void getTickers_returnsList() {
        WatchlistItem nvda = new WatchlistItem(user, "NVDA");
        WatchlistItem amd  = new WatchlistItem(user, "AMD");
        when(watchlistRepository.findByUserId(1L)).thenReturn(List.of(nvda, amd));

        List<String> tickers = watchlistService.getTickers("odgerel");

        assertThat(tickers).containsExactly("NVDA", "AMD");
    }

    @Test
    void addTicker_newTicker_saves() {
        when(watchlistRepository.existsByUserIdAndTicker(1L, "NVDA")).thenReturn(false);

        watchlistService.addTicker("odgerel", "nvda");

        verify(watchlistRepository).save(any(WatchlistItem.class));
    }

    @Test
    void addTicker_duplicate_skips() {
        when(watchlistRepository.existsByUserIdAndTicker(1L, "NVDA")).thenReturn(true);

        watchlistService.addTicker("odgerel", "NVDA");

        verify(watchlistRepository, never()).save(any());
    }

    @Test
    void addTicker_lowercaseInput_savesUppercase() {
        when(watchlistRepository.existsByUserIdAndTicker(1L, "TSLA")).thenReturn(false);

        watchlistService.addTicker("odgerel", "tsla");

        verify(watchlistRepository).save(argThat(item -> item.getTicker().equals("TSLA")));
    }

    @Test
    void removeTicker_exists_deletes() {
        WatchlistItem item = new WatchlistItem(user, "NVDA");
        when(watchlistRepository.findByUserIdAndTicker(1L, "NVDA")).thenReturn(Optional.of(item));

        watchlistService.removeTicker("odgerel", "NVDA");

        verify(watchlistRepository).delete(item);
    }

    @Test
    void removeTicker_notFound_doesNothing() {
        when(watchlistRepository.findByUserIdAndTicker(1L, "NVDA")).thenReturn(Optional.empty());

        watchlistService.removeTicker("odgerel", "NVDA");

        verify(watchlistRepository, never()).delete(any());
    }
}
