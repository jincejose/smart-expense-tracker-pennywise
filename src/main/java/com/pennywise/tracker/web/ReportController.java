package com.pennywise.tracker.web;

import com.pennywise.tracker.service.BudgetService;
import com.pennywise.tracker.service.ExpenseService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;

@Controller
public class ReportController {
    private final ExpenseService expenses;
    private final BudgetService budgets;
    public ReportController(ExpenseService expenses, BudgetService budgets) { this.expenses = expenses; this.budgets = budgets; }

    @GetMapping("/reports") public String report(@RequestParam(required = false) String month, Model model) {
        YearMonth selected = ExpenseService.parseMonth(month);
        var entries = expenses.forMonth(selected);
        BigDecimal total = expenses.total(entries);
        int elapsed = selected.equals(YearMonth.now()) ? java.time.LocalDate.now().getDayOfMonth() : selected.lengthOfMonth();
        BigDecimal forecast = total.multiply(BigDecimal.valueOf(selected.lengthOfMonth())).divide(BigDecimal.valueOf(Math.max(1, elapsed)), 2, java.math.RoundingMode.HALF_UP);
        var categoryCards = DashboardController.categoryCards(entries, budgets.byCategory()).stream().filter(card -> card.spent().signum() > 0).sorted((a,b) -> b.spent().compareTo(a.spent())).toList();
        YearMonth previousMonth = selected.minusMonths(1);
        BigDecimal previous = expenses.total(expenses.forMonth(previousMonth));
        model.addAttribute("page", "reports"); model.addAttribute("month", selected.toString());
        model.addAttribute("monthLabel", selected.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + selected.getYear());
        model.addAttribute("total", total); model.addAttribute("dailyAverage", total.divide(BigDecimal.valueOf(Math.max(1, elapsed)), 2, java.math.RoundingMode.HALF_UP));
        model.addAttribute("budgetTotal", budgets.total()); model.addAttribute("forecast", forecast); model.addAttribute("categoryCards", categoryCards);
        model.addAttribute("trend", trend(selected)); model.addAttribute("maxTrend", trend(selected).stream().map(DashboardController.TrendPoint::spent).max(BigDecimal::compareTo).orElse(BigDecimal.ONE).max(BigDecimal.ONE));
        model.addAttribute("previousTotal", previous); model.addAttribute("monthChange", previous.signum() == 0 ? BigDecimal.ZERO : total.subtract(previous).multiply(BigDecimal.valueOf(100)).divide(previous, 1, java.math.RoundingMode.HALF_UP));
        return "reports";
    }
    private java.util.List<DashboardController.TrendPoint> trend(YearMonth selected) {
        java.util.List<DashboardController.TrendPoint> points = new java.util.ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth m = selected.minusMonths(i);
            BigDecimal spent = expenses.total(expenses.forMonth(m));
            BigDecimal budget = budgets.total();
            int percent = budget.signum() == 0 ? 0 : Math.min(100, spent.multiply(BigDecimal.valueOf(100)).divide(budget, 0, java.math.RoundingMode.HALF_UP).intValue());
            points.add(new DashboardController.TrendPoint(m.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH), spent, budget, percent));
        }
        return points;
    }
}
