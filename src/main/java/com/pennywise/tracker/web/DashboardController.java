package com.pennywise.tracker.web;

import com.pennywise.tracker.model.Expense;
import com.pennywise.tracker.service.BudgetService;
import com.pennywise.tracker.service.ExpenseService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
public class DashboardController {
    private static final List<String> COLORS = List.of("#6d5ce8", "#edab6a", "#5cb796", "#ee8194", "#63a9d2", "#dbbf60", "#a78dd2", "#9aa4b2");
    private final ExpenseService expenses;
    private final BudgetService budgets;
    public DashboardController(ExpenseService expenses, BudgetService budgets) { this.expenses = expenses; this.budgets = budgets; }

    @GetMapping("/") public String overview(@RequestParam(required = false) String month, Model model) {
        YearMonth selected = ExpenseService.parseMonth(month);
        List<Expense> entries = expenses.forMonth(selected);
        BigDecimal total = expenses.total(entries);
        Map<String, BigDecimal> limits = budgets.byCategory();
        BigDecimal budgetTotal = budgets.total();
        int daysElapsed = selected.equals(YearMonth.now()) ? LocalDate.now().getDayOfMonth() : selected.lengthOfMonth();
        BigDecimal forecast = total.multiply(BigDecimal.valueOf(selected.lengthOfMonth())).divide(BigDecimal.valueOf(Math.max(1, daysElapsed)), 2, java.math.RoundingMode.HALF_UP);
        List<CategoryCard> categoryCards = categoryCards(entries, limits);
        List<Expense> previous = expenses.forMonth(selected.minusMonths(1));
        BigDecimal previousTotal = expenses.total(previous);
        List<TrendPoint> trend = trend(selected, budgetTotal);
        int alertCount = (int) categoryCards.stream().filter(card -> card.percent() >= 80).count();
        model.addAttribute("page", "overview");
        model.addAttribute("month", selected.toString());
        model.addAttribute("monthLabel", selected.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + selected.getYear());
        model.addAttribute("total", total);
        model.addAttribute("budgetTotal", budgetTotal);
        model.addAttribute("dailyAverage", total.divide(BigDecimal.valueOf(Math.max(1, daysElapsed)), 2, java.math.RoundingMode.HALF_UP));
        model.addAttribute("forecast", forecast);
        model.addAttribute("forecastOver", forecast.compareTo(budgetTotal) > 0);
        model.addAttribute("previousTotal", previousTotal);
        model.addAttribute("monthChange", previousTotal.signum() == 0 ? BigDecimal.ZERO : total.subtract(previousTotal).multiply(BigDecimal.valueOf(100)).divide(previousTotal, 1, java.math.RoundingMode.HALF_UP));
        model.addAttribute("budgetPercent", budgetTotal.signum() == 0 ? 0 : Math.min(100, total.multiply(BigDecimal.valueOf(100)).divide(budgetTotal, 0, java.math.RoundingMode.HALF_UP).intValue()));
        model.addAttribute("entries", entries.stream().limit(5).toList());
        model.addAttribute("categoryCards", categoryCards);
        model.addAttribute("alerts", categoryCards.stream().filter(card -> card.percent() >= 80).toList());
        model.addAttribute("trend", trend);
        model.addAttribute("maxTrend", trend.stream().map(TrendPoint::spent).max(BigDecimal::compareTo).orElse(BigDecimal.ONE).max(BigDecimal.ONE));
        model.addAttribute("today", LocalDate.now());
        int hour = java.time.LocalTime.now().getHour();
        model.addAttribute("greeting", hour < 12 ? "Good morning" : hour < 18 ? "Good afternoon" : "Good evening");
        return "overview";
    }

    static List<CategoryCard> categoryCards(List<Expense> entries, Map<String, BigDecimal> limits) {
        BigDecimal total = entries.stream().map(Expense::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        List<CategoryCard> result = new ArrayList<>();
        for (int i = 0; i < ExpenseService.categories().size(); i++) {
            String category = ExpenseService.categories().get(i);
            BigDecimal spent = entries.stream().filter(e -> category.equals(e.getCategory())).map(Expense::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal limit = limits.getOrDefault(category, BigDecimal.valueOf(100));
            int percent = limit.signum() == 0 ? 0 : spent.multiply(BigDecimal.valueOf(100)).divide(limit, 0, java.math.RoundingMode.HALF_UP).intValue();
            int share = total.signum() == 0 ? 0 : spent.multiply(BigDecimal.valueOf(100)).divide(total, 0, java.math.RoundingMode.HALF_UP).intValue();
            result.add(new CategoryCard(category, spent, limit, limit.subtract(spent).max(BigDecimal.ZERO), percent, share, COLORS.get(i)));
        }
        return result;
    }

    private List<TrendPoint> trend(YearMonth selected, BigDecimal budget) {
        List<TrendPoint> result = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth month = selected.minusMonths(i);
            BigDecimal spent = expenses.total(expenses.forMonth(month));
            int percent = budget.signum() == 0 ? 0 : Math.min(100, spent.multiply(BigDecimal.valueOf(100)).divide(budget, 0, java.math.RoundingMode.HALF_UP).intValue());
            result.add(new TrendPoint(month.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH), spent, budget, percent));
        }
        return result;
    }
    public record CategoryCard(String category, BigDecimal spent, BigDecimal limit, BigDecimal remaining, int percent, int share, String color) { }
    public record TrendPoint(String label, BigDecimal spent, BigDecimal budget, int percent) { }
}
