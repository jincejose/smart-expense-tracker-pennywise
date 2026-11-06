package com.pennywise.tracker.repository;

import com.pennywise.tracker.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Optional<Budget> findByCategory(String category);
}
