package com.expensetracker.api.service;

import com.expensetracker.api.controller.exception.ResourceNotFoundException;
import com.expensetracker.api.dto.CreateExpenseRequest;
import com.expensetracker.api.dto.ExpenseResponse;
import com.expensetracker.api.entity.Category;
import com.expensetracker.api.entity.Expense;
import com.expensetracker.api.entity.User;
import com.expensetracker.api.repository.CategoryRepository;
import com.expensetracker.api.repository.ExpenseRepository;
import com.expensetracker.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    public ExpenseResponse createExpense(CreateExpenseRequest request) {
        User user = getAuthenticatedUser();

        Category category = categoryRepository.findByName(request.getCategoryName())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + request.getCategoryName()));

        Expense expense = Expense.builder()
                .amount(request.getAmount())
                .description(request.getDescription())
                .category(category)
                .date(request.getDate())
                .user(user)
                .build();

        Expense saved = expenseRepository.save(expense);
        return toExpenseResponse(saved);
    }

    public List<ExpenseResponse> getAllExpenses() {
        User user = getAuthenticatedUser();
        return expenseRepository.findByUserId(user.getId())
                .stream()
                .map(this::toExpenseResponse)
                .collect(Collectors.toList());
    }

    public List<ExpenseResponse> getExpensesByCategory(String categoryName) {
        User user = getAuthenticatedUser();
        return expenseRepository.findByUserIdAndCategoryName(user.getId(), categoryName)
                .stream()
                .map(this::toExpenseResponse)
                .collect(Collectors.toList());
    }

    private ExpenseResponse toExpenseResponse(Expense expense) {
        return ExpenseResponse.builder()
                .id(expense.getId())
                .amount(expense.getAmount())
                .description(expense.getDescription())
                .categoryName(expense.getCategory().getName())
                .date(expense.getDate())
                .build();
    }
}
