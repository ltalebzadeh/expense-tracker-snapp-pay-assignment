package com.expensetracker.api.service;

import com.expensetracker.api.controller.dto.CreateExpenseRequest;
import com.expensetracker.api.controller.dto.ExpenseResponse;
import com.expensetracker.api.entity.Category;
import com.expensetracker.api.entity.Expense;
import com.expensetracker.api.entity.User;
import com.expensetracker.api.repository.CategoryRepository;
import com.expensetracker.api.repository.ExpenseRepository;
import com.expensetracker.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public ExpenseResponse createExpense(CreateExpenseRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Category category = categoryRepository.findByName(request.getCategoryName())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Expense expense = Expense.builder()
                .amount(request.getAmount())
                .description(request.getDescription())
                .date(request.getDate())
                .category(category)
                .user(user)
                .build();

        Expense saved = expenseRepository.save(expense);

        return ExpenseResponse.builder()
                .id(saved.getId())
                .amount(saved.getAmount())
                .description(saved.getDescription())
                .categoryName(saved.getCategory().getName())
                .date(saved.getDate())
                .build();
    }
}
