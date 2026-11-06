package com.pennywise.tracker.web;

import com.pennywise.tracker.model.Expense;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import java.math.BigDecimal;
import java.time.LocalDate;

public class ExpenseForm {
    @NotBlank @Size(max = 100) private String title;
    @NotBlank @Size(max = 40) private String category;
    @NotNull @DecimalMin("0.01") private BigDecimal amount;
    @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) private LocalDate spentOn = LocalDate.now();
    @NotBlank @Size(max = 32) private String paymentMethod = "Card";
    @Size(max = 200) private String note = "";

    public Expense toExpense() { return new Expense(title.trim(), category, amount, spentOn, paymentMethod, note); }
    public void copyFrom(Expense expense) { title = expense.getTitle(); category = expense.getCategory(); amount = expense.getAmount(); spentOn = expense.getSpentOn(); paymentMethod = expense.getPaymentMethod(); note = expense.getNote(); }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDate getSpentOn() { return spentOn; }
    public void setSpentOn(LocalDate spentOn) { this.spentOn = spentOn; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
