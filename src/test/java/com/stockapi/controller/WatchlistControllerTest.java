package com.stockapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockapi.security.JwtUtil;
import com.stockapi.security.UserDetailsServiceImpl;
import com.stockapi.service.WatchlistService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WatchlistController.class)
@Import(TestSecurityConfig.class)
class WatchlistControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean WatchlistService watchlistService;
    @MockBean JwtUtil jwtUtil;
    @MockBean UserDetailsServiceImpl userDetailsService;

    @Test
    @WithMockUser(username = "odgerel")
    void list_authenticated_returnsTickers() throws Exception {
        when(watchlistService.getTickers("odgerel")).thenReturn(List.of("NVDA", "AMD"));

        mockMvc.perform(get("/api/watchlist"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0]").value("NVDA"))
            .andExpect(jsonPath("$[1]").value("AMD"));
    }

    @Test
    void list_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/watchlist"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "odgerel")
    void add_validTicker_returns200() throws Exception {
        mockMvc.perform(post("/api/watchlist")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("ticker", "NVDA"))))
            .andExpect(status().isOk());

        verify(watchlistService).addTicker("odgerel", "NVDA");
    }

    @Test
    @WithMockUser(username = "odgerel")
    void add_missingTicker_returns400() throws Exception {
        mockMvc.perform(post("/api/watchlist")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "odgerel")
    void remove_ticker_returns204() throws Exception {
        mockMvc.perform(delete("/api/watchlist/NVDA"))
            .andExpect(status().isNoContent());

        verify(watchlistService).removeTicker("odgerel", "NVDA");
    }
}
