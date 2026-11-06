package com.pennywise.tracker.service;

import com.pennywise.tracker.model.Budget;
import com.pennywise.tracker.repository.BudgetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class BudgetService {
    private final BudgetRepository repository;
    public BudgetService(BudgetRepository repository) { this.repository = repository; }
    public List<Budget> all() { return repository.findAll().stream().sorted((a,b) -> a.getCategory().compareTo(b.getCategory())).toList(); }
    public Map<String, BigDecimal> byCategory() {
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        for (String category : ExpenseService.categories()) result.put(category, repository.findByCategory(category).map(Budget::getAmount).orElse(BigDecimal.valueOf(100)));
        return result;
    }
    public BigDecimal total() { return byCategory().values().stream().reduce(BigDecimal.ZERO, BigDecimal::add); }
    @Transactional public void save(Map<String, BigDecimal> values) {
        for (String category : ExpenseService.categories()) {
            BigDecimal amount = values.get(category);
            if (amount == null || amount.signum() <= 0) throw new IllegalArgumentException("Every category budget must be greater than zero.");
            Budget budget = repository.findByCategory(category).orElseGet(() -> new Budget(category, amount));
            budget.setAmount(amount);
            repository.save(budget);
        }
    }
}
