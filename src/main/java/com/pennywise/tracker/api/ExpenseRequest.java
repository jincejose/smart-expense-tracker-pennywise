package com.pennywise.tracker.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseRequest(
        @NotBlank @Size(max = 100) String title,
        @NotBlank @Size(max = 40) String category,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @NotNull LocalDate spentOn,
        @Size(max = 32) String paymentMethod,
        @Size(max = 200) String note) { }
