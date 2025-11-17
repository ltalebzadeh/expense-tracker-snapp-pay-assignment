package com.expensetracker.api.controller;

import com.expensetracker.api.controller.dto.CreateExpenseRequest;
import com.expensetracker.api.controller.dto.ExpenseResponse;
import com.expensetracker.api.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    public ExpenseResponse createExpense(@RequestBody CreateExpenseRequest request) {
        return expenseService.createExpense(request);
    }
}