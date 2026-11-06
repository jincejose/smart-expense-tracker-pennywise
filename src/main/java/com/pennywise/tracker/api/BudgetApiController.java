package com.pennywise.tracker.api;

import com.pennywise.tracker.model.Budget;
import com.pennywise.tracker.service.BudgetService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/budgets")
public class BudgetApiController {
    private final BudgetService service;
    public BudgetApiController(BudgetService service) { this.service = service; }
    @GetMapping public List<BudgetResponse> list() { return service.all().stream().map(BudgetResponse::from).toList(); }
    @PutMapping public List<BudgetResponse> save(@Valid @RequestBody BudgetRequest request) {
        service.save(request.limits());
        return service.all().stream().map(BudgetResponse::from).toList();
    }
    public record BudgetRequest(@Valid Map<@NotBlank String, @DecimalMin("0.01") BigDecimal> limits) { }
    public record BudgetResponse(String category, BigDecimal amount) {
        static BudgetResponse from(Budget budget) { return new BudgetResponse(budget.getCategory(), budget.getAmount()); }
    }
}
