package com.expensetracker.api.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class ExpenseResponse {
    private Long id;
    private BigDecimal amount;
    private String description;
    private String categoryName;
    private LocalDate date;
}