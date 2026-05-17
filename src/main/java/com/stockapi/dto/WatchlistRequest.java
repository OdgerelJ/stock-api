package com.stockapi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WatchlistRequest {
    @NotBlank
    private String ticker;
}
