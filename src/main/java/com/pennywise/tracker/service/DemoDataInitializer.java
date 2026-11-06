package com.pennywise.tracker.service;

import com.pennywise.tracker.model.Expense;
import com.pennywise.tracker.repository.BudgetRepository;
import com.pennywise.tracker.repository.ExpenseRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Configuration
public class DemoDataInitializer {
    @Bean ApplicationRunner seedDemoData(ExpenseRepository expenses, BudgetRepository budgets) {
        return args -> {
            if (budgets.count() == 0) {
                int[] amounts = {480, 300, 220, 140, 160, 240, 300, 100};
                for (int i = 0; i < ExpenseService.categories().size(); i++) budgets.save(new com.pennywise.tracker.model.Budget(ExpenseService.categories().get(i), BigDecimal.valueOf(amounts[i])));
            }
            if (expenses.count() == 0) {
                LocalDate today = LocalDate.now();
                List<Expense> demo = List.of(
                    new Expense("Weekly groceries", "Food & dining", new BigDecimal("84.32"), today, "Card", "Fresh Market"),
                    new Expense("Coffee with a friend", "Food & dining", new BigDecimal("16.50"), today.minusDays(1), "Card", "Cafe Bloom"),
                    new Expense("Monthly subscription", "Entertainment", new BigDecimal("15.99"), today.minusDays(2), "Card", "Streaming"),
                    new Expense("Ride to work", "Transport", new BigDecimal("18.75"), today.minusDays(3), "Card", "Metro"),
                    new Expense("New desk lamp", "Shopping", new BigDecimal("46.00"), today.minusDays(5), "Card", "Home & Co."),
                    new Expense("Electric bill", "Bills & utilities", new BigDecimal("67.40"), today.minusDays(8), "Bank transfer", "Monthly utilities"),
                    new Expense("Yoga class", "Health", new BigDecimal("22.00"), today.minusDays(10), "Card", "Weekly class"),
                    new Expense("Weekend getaway", "Travel", new BigDecimal("145.00"), today.minusDays(18), "Card", "Hotel deposit"));
                expenses.saveAll(demo);
            }
        };
    }
}
