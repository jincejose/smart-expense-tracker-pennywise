package com.pennywise.tracker.web;

import com.pennywise.tracker.model.Expense;
import com.pennywise.tracker.service.ExpenseService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class TransactionController {
    private final ExpenseService expenses;
    public TransactionController(ExpenseService expenses) { this.expenses = expenses; }

    @GetMapping("/transactions") public String transactions(@RequestParam(required = false) String month,
            @RequestParam(required = false) String category, @RequestParam(required = false) String search, Model model) {
        YearMonth selected = ExpenseService.parseMonth(month);
        List<Expense> filtered = expenses.filter(selected, category, search);
        model.addAttribute("page", "transactions");
        model.addAttribute("month", selected.toString());
        model.addAttribute("monthLabel", selected.getMonth().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH) + " " + selected.getYear());
        model.addAttribute("category", category == null ? "All categories" : category);
        model.addAttribute("search", search == null ? "" : search);
        model.addAttribute("categories", ExpenseService.categories());
        model.addAttribute("entries", filtered);
        model.addAttribute("total", expenses.total(filtered));
        model.addAttribute("count", filtered.size());
        return "transactions";
    }

    @GetMapping("/expenses/new") public String newExpense(Model model) {
        model.addAttribute("page", "transactions");
        model.addAttribute("form", new ExpenseForm());
        model.addAttribute("categories", ExpenseService.categories());
        model.addAttribute("paymentMethods", ExpenseService.paymentMethods());
        model.addAttribute("formTitle", "Add an expense");
        model.addAttribute("formAction", "/expenses");
        return "expense-form";
    }
    @GetMapping("/expenses/{id}/edit") public String editExpense(@PathVariable Long id, Model model) {
        ExpenseForm form = new ExpenseForm(); form.copyFrom(expenses.get(id));
        model.addAttribute("page", "transactions");
        model.addAttribute("form", form);
        model.addAttribute("categories", ExpenseService.categories());
        model.addAttribute("paymentMethods", ExpenseService.paymentMethods());
        model.addAttribute("formTitle", "Edit expense");
        model.addAttribute("formAction", "/expenses/" + id + "/edit");
        return "expense-form";
    }
    @PostMapping("/expenses") public String create(@ModelAttribute("form") @jakarta.validation.Valid ExpenseForm form,
                                                       org.springframework.validation.BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) { prepareForm(model, form, "Add an expense", "/expenses"); return "expense-form"; }
        expenses.create(form.toExpense()); redirect.addFlashAttribute("success", "Expense added."); return "redirect:/transactions";
    }
    @PostMapping("/expenses/{id}/edit") public String update(@PathVariable Long id, @ModelAttribute("form") @jakarta.validation.Valid ExpenseForm form,
            org.springframework.validation.BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) { prepareForm(model, form, "Edit expense", "/expenses/" + id + "/edit"); return "expense-form"; }
        expenses.update(id, form); redirect.addFlashAttribute("success", "Expense updated."); return "redirect:/transactions";
    }
    @PostMapping("/expenses/{id}/delete") public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        expenses.delete(id); redirect.addFlashAttribute("success", "Expense deleted."); return "redirect:/transactions";
    }
    @GetMapping(value = "/reports/export", produces = "text/csv") public org.springframework.http.ResponseEntity<byte[]> export(@RequestParam(required = false) String month) {
        YearMonth selected = ExpenseService.parseMonth(month);
        String csv = "Date,Description,Category,Amount,Payment,Note\r\n" + expenses.forMonth(selected).stream().map(this::csvLine).collect(Collectors.joining("\r\n"));
        return org.springframework.http.ResponseEntity.ok().header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=pennywise-" + selected + ".csv").body(csv.getBytes(StandardCharsets.UTF_8));
    }
    private String csvLine(Expense e) { return List.of(e.getSpentOn().toString(), e.getTitle(), e.getCategory(), e.getAmount().toPlainString(), e.getPaymentMethod(), e.getNote()).stream().map(this::quote).collect(Collectors.joining(",")); }
    private String quote(String value) { return "\"" + value.replace("\"", "\"\"") + "\""; }
    private void prepareForm(Model model, ExpenseForm form, String title, String action) {
        model.addAttribute("page", "transactions"); model.addAttribute("form", form); model.addAttribute("categories", ExpenseService.categories());
        model.addAttribute("paymentMethods", ExpenseService.paymentMethods()); model.addAttribute("formTitle", title); model.addAttribute("formAction", action);
    }
}
