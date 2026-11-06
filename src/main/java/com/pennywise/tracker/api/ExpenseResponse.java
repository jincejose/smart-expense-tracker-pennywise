package com.pennywise.tracker.api;

import com.pennywise.tracker.model.Expense;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseResponse(Long id, String title, String category, BigDecimal amount, LocalDate spentOn, String paymentMethod, String note) {
    public static ExpenseResponse from(Expense expense) {
        return new ExpenseResponse(expense.getId(), expense.getTitle(), expense.getCategory(), expense.getAmount(), expense.getSpentOn(), expense.getPaymentMethod(), expense.getNote());
    }
}
