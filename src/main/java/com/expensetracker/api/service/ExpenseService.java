package com.expensetracker.api.service;

import com.expensetracker.api.controller.dto.CreateExpenseRequest;
import com.expensetracker.api.controller.dto.ExpenseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    public ExpenseResponse createExpense(CreateExpenseRequest request) {
        return ExpenseResponse.builder().build();
    }
}
