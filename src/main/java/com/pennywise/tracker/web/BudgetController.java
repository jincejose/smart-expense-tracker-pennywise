package com.pennywise.tracker.web;

import com.pennywise.tracker.model.Expense;
import com.pennywise.tracker.service.BudgetService;
import com.pennywise.tracker.service.ExpenseService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Map;

@Controller
public class BudgetController {
    private final BudgetService budgets;
    private final ExpenseService expenses;
    public BudgetController(BudgetService budgets, ExpenseService expenses) { this.budgets = budgets; this.expenses = expenses; }

    @GetMapping("/budgets") public String budgets(Model model) {
        YearMonth month = YearMonth.now();
        Map<String, BigDecimal> limits = budgets.byCategory();
        var entries = expenses.forMonth(month);
        var categories = DashboardController.categoryCards(entries, limits);
        BudgetForm form = new BudgetForm(); form.setLimits(limits);
        BigDecimal spent = expenses.total(entries);
        BigDecimal totalLimit = budgets.total();
        model.addAttribute("page", "budgets"); model.addAttribute("month", month.toString()); model.addAttribute("monthLabel", month.getMonth().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH));
        model.addAttribute("categoryCards", categories); model.addAttribute("limits", limits); model.addAttribute("totalLimit", totalLimit);
        model.addAttribute("budgetForm", form);
        model.addAttribute("spent", spent); model.addAttribute("remaining", totalLimit.subtract(spent).max(BigDecimal.ZERO));
        model.addAttribute("budgetPercent", totalLimit.signum() == 0 ? 0 : Math.min(100, spent.multiply(BigDecimal.valueOf(100)).divide(totalLimit, 0, java.math.RoundingMode.HALF_UP).intValue()));
        model.addAttribute("alerts", categories.stream().filter(card -> card.percent() >= 80).toList());
        return "budgets";
    }
    @PostMapping("/budgets") public String save(@ModelAttribute BudgetForm form, RedirectAttributes redirect) {
        try { budgets.save(form.getLimits()); redirect.addFlashAttribute("success", "Monthly budgets saved."); }
        catch (IllegalArgumentException ex) { redirect.addFlashAttribute("error", ex.getMessage()); }
        return "redirect:/budgets";
    }
}
