package com.stockapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PositionResponse {
    private Long id;
    private String ticker;
    private double quantity;
    private double avgCost;
    private Double targetPrice;
    private Double stopPrice;
}
