package com.expensetracker.api.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateExpenseRequest {
    private BigDecimal amount;
    private LocalDate date;
    private String description;
    private String categoryName;
}