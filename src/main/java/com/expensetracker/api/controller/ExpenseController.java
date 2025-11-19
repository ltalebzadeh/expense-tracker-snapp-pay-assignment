package com.expensetracker.api.controller;

import com.expensetracker.api.dto.CreateExpenseRequest;
import com.expensetracker.api.dto.ExpenseResponse;
import com.expensetracker.api.dto.MonthlyReportResponse;
import com.expensetracker.api.dto.UpdateExpenseRequest;
import com.expensetracker.api.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    public ExpenseResponse createExpense(@Valid @RequestBody CreateExpenseRequest request) {
        return expenseService.createExpense(request);
    }

    @GetMapping
    public List<ExpenseResponse> getAllExpenses() {
        return expenseService.getAllExpenses();
    }

    @GetMapping("/category/{categoryName}")
    public List<ExpenseResponse> getExpensesByCategory(@PathVariable String categoryName) {
        return expenseService.getExpensesByCategory(categoryName);
    }

    @PutMapping("/{id}")
    public ExpenseResponse updateExpense(@PathVariable Long id, @Valid @RequestBody UpdateExpenseRequest request) {
        return expenseService.updateExpense(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
    }

    @GetMapping("/report")
    public MonthlyReportResponse getMonthlyReport(@RequestParam int year, @RequestParam int month) {
        return expenseService.getMonthlyReport(year, month);
    }
}