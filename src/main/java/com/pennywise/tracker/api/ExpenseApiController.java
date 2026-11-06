package com.pennywise.tracker.api;

import com.pennywise.tracker.model.Expense;
import com.pennywise.tracker.service.ExpenseService;
import com.pennywise.tracker.web.ExpenseForm;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseApiController {
    private final ExpenseService service;
    public ExpenseApiController(ExpenseService service) { this.service = service; }

    @GetMapping public List<ExpenseResponse> list(@RequestParam(required = false) String month,
                                                   @RequestParam(required = false) String category,
                                                   @RequestParam(required = false) String search) {
        return service.filter(ExpenseService.parseMonth(month), category, search).stream().map(ExpenseResponse::from).toList();
    }
    @GetMapping("/{id}") public ExpenseResponse get(@PathVariable Long id) { return ExpenseResponse.from(service.get(id)); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public ExpenseResponse create(@Valid @RequestBody ExpenseRequest request) {
        return ExpenseResponse.from(service.create(toExpense(request)));
    }
    @PutMapping("/{id}") public ExpenseResponse update(@PathVariable Long id, @Valid @RequestBody ExpenseRequest request) {
        ExpenseForm form = new ExpenseForm();
        form.setTitle(request.title()); form.setCategory(request.category()); form.setAmount(request.amount()); form.setSpentOn(request.spentOn());
        form.setPaymentMethod(request.paymentMethod() == null || request.paymentMethod().isBlank() ? "Card" : request.paymentMethod()); form.setNote(request.note() == null ? "" : request.note());
        return ExpenseResponse.from(service.update(id, form));
    }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id) { service.delete(id); }
    private Expense toExpense(ExpenseRequest request) {
        return new Expense(request.title().trim(), request.category(), request.amount(), request.spentOn(), request.paymentMethod(), request.note());
    }
}
