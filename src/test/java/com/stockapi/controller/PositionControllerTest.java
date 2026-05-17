package com.stockapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockapi.dto.PositionRequest;
import com.stockapi.dto.PositionResponse;
import com.stockapi.security.JwtUtil;
import com.stockapi.security.UserDetailsServiceImpl;
import com.stockapi.service.PositionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PositionController.class)
@Import(TestSecurityConfig.class)
class PositionControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean PositionService positionService;
    @MockBean JwtUtil jwtUtil;
    @MockBean UserDetailsServiceImpl userDetailsService;

    @Test
    @WithMockUser(username = "odgerel")
    void list_authenticated_returnsPositions() throws Exception {
        PositionResponse pos = new PositionResponse(1L, "NVDA", 10, 850.0, null, null);

        when(positionService.getPositions("odgerel")).thenReturn(List.of(pos));

        mockMvc.perform(get("/api/positions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].ticker").value("NVDA"))
            .andExpect(jsonPath("$[0].quantity").value(10))
            .andExpect(jsonPath("$[0].avgCost").value(850.0));
    }

    @Test
    void list_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/positions"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "odgerel")
    void save_validRequest_returns200() throws Exception {
        PositionRequest req = new PositionRequest();
        req.setTicker("NVDA");
        req.setQuantity(10);
        req.setAvgCost(850.0);

        PositionResponse saved = new PositionResponse(1L, "NVDA", 10, 850.0, null, null);
        when(positionService.save(eq("odgerel"), any())).thenReturn(saved);

        mockMvc.perform(put("/api/positions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ticker").value("NVDA"));
    }

    @Test
    @WithMockUser(username = "odgerel")
    void save_negativeQuantity_returns400() throws Exception {
        PositionRequest req = new PositionRequest();
        req.setTicker("NVDA");
        req.setQuantity(-5); // invalid
        req.setAvgCost(850.0);

        mockMvc.perform(put("/api/positions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "odgerel")
    void delete_ticker_returns204() throws Exception {
        mockMvc.perform(delete("/api/positions/NVDA"))
            .andExpect(status().isNoContent());

        verify(positionService).delete("odgerel", "NVDA");
    }
}
