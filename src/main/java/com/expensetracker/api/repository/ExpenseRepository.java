package com.expensetracker.api.repository;

import com.expensetracker.api.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByUserId(Long userId);
    List<Expense> findByUserIdAndCategoryName(Long userId, String categoryName);
    Optional<Expense> findByIdAndUserId(Long id, Long userId);
}