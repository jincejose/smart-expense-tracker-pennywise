package com.pennywise.tracker.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "expenses", indexes = @Index(name = "idx_expenses_spent_on", columnList = "spent_on"))
public class Expense {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String title;
    @Column(nullable = false, length = 40)
    private String category;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;
    @Column(name = "spent_on", nullable = false)
    private LocalDate spentOn;
    @Column(name = "payment_method", nullable = false, length = 32)
    private String paymentMethod = "Card";
    @Column(length = 200)
    private String note = "";

    protected Expense() { }

    public Expense(String title, String category, BigDecimal amount, LocalDate spentOn, String paymentMethod, String note) {
        this.title = title;
        this.category = category;
        this.amount = amount;
        this.spentOn = spentOn;
        this.paymentMethod = paymentMethod == null || paymentMethod.isBlank() ? "Card" : paymentMethod;
        this.note = note == null ? "" : note;
    }

    public void update(String title, String category, BigDecimal amount, LocalDate spentOn, String paymentMethod, String note) {
        this.title = title;
        this.category = category;
        this.amount = amount;
        this.spentOn = spentOn;
        this.paymentMethod = paymentMethod == null || paymentMethod.isBlank() ? "Card" : paymentMethod;
        this.note = note == null ? "" : note;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public BigDecimal getAmount() { return amount; }
    public LocalDate getSpentOn() { return spentOn; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getNote() { return note; }
}
