package com.expensetracker.api.controller.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateExpenseRequest {
    private BigDecimal amount;
    private LocalDate date;
    private String description;
    private String categoryName;
    private String username;
}
