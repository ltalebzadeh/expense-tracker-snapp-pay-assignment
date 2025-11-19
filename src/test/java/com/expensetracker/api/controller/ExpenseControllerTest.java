package com.expensetracker.api.controller;

import com.expensetracker.api.controller.exception.CustomExceptionHandler;
import com.expensetracker.api.controller.exception.ResourceNotFoundException;
import com.expensetracker.api.dto.CreateExpenseRequest;
import com.expensetracker.api.dto.ExpenseResponse;
import com.expensetracker.api.dto.MonthlyReportResponse;
import com.expensetracker.api.dto.UpdateExpenseRequest;
import com.expensetracker.api.service.ExpenseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExpenseController.class)
@Import(CustomExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ExpenseService expenseService;

    private CreateExpenseRequest request;

    @BeforeEach
    void setUp() {
        request = new CreateExpenseRequest();
    }

    @Test
    @WithMockUser(username = "broke_developer")
    void createExpense_Success() throws Exception {
        request.setAmount(BigDecimal.valueOf(420.5));
        request.setDescription("Pizza at 3 AM");
        request.setCategoryName("Food");
        request.setDate(LocalDate.of(2025, 11, 18));

        ExpenseResponse response = ExpenseResponse.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(420.5))
                .description("Pizza at 3 AM")
                .categoryName("Food")
                .date(LocalDate.of(2025, 11, 18))
                .build();

        when(expenseService.createExpense(any(CreateExpenseRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(420.5))
                .andExpect(jsonPath("$.description").value("Pizza at 3 AM"))
                .andExpect(jsonPath("$.categoryName").value("Food"));
    }

    @Test
    @WithMockUser(username = "broke_developer")
    void createExpense_CategoryNotFound_ReturnsCategoryNotFound() throws Exception {
        request.setCategoryName("Luxury");
        request.setAmount(BigDecimal.valueOf(50000.00));
        request.setDate(LocalDate.now());

        when(expenseService.createExpense(any(CreateExpenseRequest.class)))
                .thenThrow(new ResourceNotFoundException("Category not found: Luxury"));

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Category not found: Luxury"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @WithMockUser(username = "broke_developer")
    void createExpense_GenericException_ReturnsInternalServerError() throws Exception {
        request.setAmount(BigDecimal.valueOf(420.5));
        request.setDescription("Pizza at 3 AM");
        request.setCategoryName("Food");
        request.setDate(LocalDate.of(2025, 11, 18));

        when(expenseService.createExpense(any(CreateExpenseRequest.class)))
                .thenThrow(new RuntimeException("System is done"));

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Internal server error"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @WithMockUser(username = "broke_developer")
    void getAllExpenses_Success() throws Exception {
        List<ExpenseResponse> responses = List.of(
                ExpenseResponse.builder()
                        .id(1L)
                        .amount(BigDecimal.valueOf(50.00))
                        .description("Omelette")
                        .categoryName("Food")
                        .date(LocalDate.of(2025, 11, 18))
                        .build(),
                ExpenseResponse.builder()
                        .id(2L)
                        .amount(BigDecimal.valueOf(20.00))
                        .description("Metro")
                        .categoryName("Transport")
                        .date(LocalDate.of(2025, 11, 19))
                        .build()
        );

        when(expenseService.getAllExpenses()).thenReturn(responses);

        mockMvc.perform(get("/api/expenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].description").value("Omelette"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].description").value("Metro"));
    }

    @Test
    @WithMockUser(username = "broke_developer")
    void getExpensesByCategory_Success() throws Exception {
        List<ExpenseResponse> responses = List.of(
                ExpenseResponse.builder()
                        .id(1L)
                        .amount(BigDecimal.valueOf(100.00))
                        .description("Instant spicy noodles")
                        .categoryName("Food")
                        .date(LocalDate.of(2025, 11, 19))
                        .build()
        );

        when(expenseService.getExpensesByCategory("Food")).thenReturn(responses);

        mockMvc.perform(get("/api/expenses/category/Food"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].description").value("Instant spicy noodles"))
                .andExpect(jsonPath("$[0].categoryName").value("Food"));
    }

    @Test
    @WithMockUser(username = "broke_developer")
    void updateExpense_Success() throws Exception {
        UpdateExpenseRequest request = new UpdateExpenseRequest();
        request.setAmount(BigDecimal.valueOf(200.00));
        request.setDescription("Energy drinks");
        request.setCategoryName("Food");
        request.setDate(LocalDate.of(2025, 11, 19));

        ExpenseResponse response = ExpenseResponse.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(200.00))
                .description("Energy drinks")
                .categoryName("Food")
                .date(LocalDate.of(2025, 11, 19))
                .build();

        when(expenseService.updateExpense(eq(1L), any(UpdateExpenseRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/expenses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(200.00))
                .andExpect(jsonPath("$.description").value("Energy drinks"));
    }

    @Test
    @WithMockUser(username = "broke_developer")
    void deleteExpense_Success() throws Exception {
        doNothing().when(expenseService).deleteExpense(1L);

        mockMvc.perform(delete("/api/expenses/1"))
                .andExpect(status().isOk());

        verify(expenseService).deleteExpense(1L);
    }

    @Test
    @WithMockUser(username = "broke_developer")
    void deleteExpense_NotOwnedByUser_ReturnsNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Expense not found: 999"))
                .when(expenseService).deleteExpense(999L);

        mockMvc.perform(delete("/api/expenses/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Expense not found: 999"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @WithMockUser(username = "broke_developer")
    void createExpense_InvalidArguments_ReturnsValidationFailed() throws Exception {
        request.setAmount(BigDecimal.valueOf(-420.5));
        request.setDescription("Pizza at 3 AM");
        request.setCategoryName("");
        request.setDate(null);

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.date").value("Date is required"))
                .andExpect(jsonPath("$.errors.amount").value("Amount must be positive"))
                .andExpect(jsonPath("$.errors.categoryName").value("Category name is required"));
    }

    @Test
    @WithMockUser(username = "broke_developer")
    void getMonthlyReport_Success() throws Exception {
        Map<String, BigDecimal> spendingByCategory = new HashMap<>();
        spendingByCategory.put("Coffee", BigDecimal.valueOf(2300.00));
        spendingByCategory.put("Food", BigDecimal.valueOf(500.00));

        MonthlyReportResponse report = MonthlyReportResponse.builder()
                .year(2025)
                .month(11)
                .totalAmount(BigDecimal.valueOf(2800.00))
                .expenseCount(3)
                .spendingByCategory(spendingByCategory)
                .alerts(List.of("Spending too much money on coffee... like a usual developer."))
                .build();

        when(expenseService.getMonthlyReport(2025, 11)).thenReturn(report);

        mockMvc.perform(get("/api/expenses/report")
                        .param("year", "2025")
                        .param("month", "11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(2025))
                .andExpect(jsonPath("$.month").value(11))
                .andExpect(jsonPath("$.totalAmount").value(2800.00))
                .andExpect(jsonPath("$.expenseCount").value(3))
                .andExpect(jsonPath("$.spendingByCategory.Coffee").value(2300.00))
                .andExpect(jsonPath("$.spendingByCategory.Food").value(500.00))
                .andExpect(jsonPath("$.alerts[0]").value("Spending too much money on coffee... like a usual developer."));
    }
}