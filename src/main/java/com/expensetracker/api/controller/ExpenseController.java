package com.expensetracker.api.controller;

import com.expensetracker.api.dto.CreateExpenseRequest;
import com.expensetracker.api.dto.ExpenseResponse;
import com.expensetracker.api.dto.MonthlyReportResponse;
import com.expensetracker.api.dto.UpdateExpenseRequest;
import com.expensetracker.api.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@Tag(name = "Expenses", description = "Expense management endpoints")
@SecurityRequirement(name = "basicAuth")
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    @Operation(summary = "Create a new expense", description = "Creates a new expense for the authenticated user")
    public ExpenseResponse createExpense(@Valid @RequestBody CreateExpenseRequest request) {
        return expenseService.createExpense(request);
    }

    @GetMapping
    @Operation(summary = "Get all expenses", description = "Retrieves all expenses for the authenticated user")
    public List<ExpenseResponse> getAllExpenses() {
        return expenseService.getAllExpenses();
    }

    @GetMapping("/category/{categoryName}")
    @Operation(summary = "Get expenses by category", description = "Filters expenses by category name")
    public List<ExpenseResponse> getExpensesByCategory(@PathVariable String categoryName) {
        return expenseService.getExpensesByCategory(categoryName);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an expense", description = "Updates an existing expense")
    public ExpenseResponse updateExpense(@PathVariable Long id, @Valid @RequestBody UpdateExpenseRequest request) {
        return expenseService.updateExpense(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an expense", description = "Deletes an expense by ID")
    public void deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
    }

    @GetMapping("/report")
    @Operation(summary = "Get monthly report", description = "Generates a spending report for a specific month")
    public MonthlyReportResponse getMonthlyReport(@RequestParam int year, @RequestParam int month) {
        return expenseService.getMonthlyReport(year, month);
    }
}