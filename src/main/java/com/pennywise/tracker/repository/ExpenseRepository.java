package com.pennywise.tracker.repository;

import com.pennywise.tracker.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findAllByOrderBySpentOnDescIdDesc();
    List<Expense> findAllBySpentOnBetweenOrderBySpentOnDescIdDesc(LocalDate from, LocalDate to);
}
