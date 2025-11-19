package com.expensetracker.api.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class MonthlyReportResponse {
    private int year;
    private int month;
    private BigDecimal totalAmount;
    private int expenseCount;
    private Map<String, BigDecimal> spendingByCategory;
    private List<String> alerts;
}