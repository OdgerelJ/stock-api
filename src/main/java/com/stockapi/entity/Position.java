package com.stockapi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "positions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "ticker"})
})
@Getter @Setter @NoArgsConstructor
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String ticker;

    private double quantity;
    private double avgCost;
    private Double targetPrice;
    private Double stopPrice;

    public Position(User user, String ticker, double quantity, double avgCost) {
        this.user = user;
        this.ticker = ticker.toUpperCase();
        this.quantity = quantity;
        this.avgCost = avgCost;
    }
}
