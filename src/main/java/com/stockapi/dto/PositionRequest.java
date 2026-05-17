package com.stockapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class PositionRequest {
    @NotBlank
    private String ticker;

    @Positive
    private double quantity;

    @Positive
    private double avgCost;

    private Double targetPrice;
    private Double stopPrice;
}
