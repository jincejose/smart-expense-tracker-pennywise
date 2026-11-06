package com.pennywise.tracker.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "budgets")
public class Budget {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 40)
    private String category;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    protected Budget() { }
    public Budget(String category, BigDecimal amount) { this.category = category; this.amount = amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public Long getId() { return id; }
    public String getCategory() { return category; }
    public BigDecimal getAmount() { return amount; }
}
