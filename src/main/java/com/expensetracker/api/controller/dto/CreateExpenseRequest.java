package com.expensetracker.api.controller.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateExpenseRequest {
    private BigDecimal amount;
    private String description;
    private Long categoryId;
    private LocalDate date;
    private Long userId;
}
