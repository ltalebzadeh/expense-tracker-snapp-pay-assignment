package com.expensetracker.api.controller;

import com.expensetracker.api.controller.exception.CustomExceptionHandler;
import com.expensetracker.api.controller.exception.ResourceNotFoundException;
import com.expensetracker.api.dto.CreateExpenseRequest;
import com.expensetracker.api.dto.ExpenseResponse;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
}