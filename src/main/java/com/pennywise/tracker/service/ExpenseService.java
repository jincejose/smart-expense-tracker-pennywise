package com.pennywise.tracker.service;

import com.pennywise.tracker.model.Expense;
import com.pennywise.tracker.repository.ExpenseRepository;
import com.pennywise.tracker.web.ExpenseForm;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Locale;

@Service
public class ExpenseService {
    private final ExpenseRepository repository;
    public ExpenseService(ExpenseRepository repository) { this.repository = repository; }

    public List<Expense> all() { return repository.findAllByOrderBySpentOnDescIdDesc(); }
    public List<Expense> forMonth(YearMonth month) { return repository.findAllBySpentOnBetweenOrderBySpentOnDescIdDesc(month.atDay(1), month.atEndOfMonth()); }
    public List<Expense> filter(YearMonth month, String category, String query) {
        String normalized = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        return forMonth(month).stream()
                .filter(e -> category == null || category.isBlank() || category.equals("All categories") || e.getCategory().equals(category))
                .filter(e -> normalized.isEmpty() || (e.getTitle() + " " + e.getCategory() + " " + e.getNote()).toLowerCase(Locale.ROOT).contains(normalized))
                .toList();
    }
    public BigDecimal total(List<Expense> expenses) { return expenses.stream().map(Expense::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add); }
    public Expense get(Long id) { return repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found")); }
    @Transactional public Expense create(Expense expense) { return repository.save(expense); }
    @Transactional public Expense update(Long id, ExpenseForm form) {
        Expense expense = get(id);
        expense.update(form.getTitle().trim(), form.getCategory(), form.getAmount(), form.getSpentOn(), form.getPaymentMethod(), form.getNote());
        return repository.save(expense);
    }
    @Transactional public Expense update(Long id, Expense replacement) {
        Expense current = get(id);
        current.update(replacement.getTitle(), replacement.getCategory(), replacement.getAmount(), replacement.getSpentOn(), replacement.getPaymentMethod(), replacement.getNote());
        return repository.save(current);
    }
    @Transactional public void delete(Long id) { repository.delete(get(id)); }
    public static YearMonth parseMonth(String value) {
        if (value == null || value.isBlank()) return YearMonth.now();
        try { return YearMonth.parse(value); } catch (RuntimeException ex) { return YearMonth.now(); }
    }
    public static List<String> categories() { return List.of("Food & dining", "Shopping", "Transport", "Entertainment", "Health", "Bills & utilities", "Travel", "Other"); }
    public static List<String> paymentMethods() { return List.of("Card", "Cash", "Bank transfer", "Digital wallet", "Other"); }
}
