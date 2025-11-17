package com.expensetracker.api.service;

import com.expensetracker.api.controller.exception.ResourceNotFoundException;
import com.expensetracker.api.dto.CreateExpenseRequest;
import com.expensetracker.api.dto.ExpenseResponse;
import com.expensetracker.api.entity.Category;
import com.expensetracker.api.entity.Expense;
import com.expensetracker.api.entity.User;
import com.expensetracker.api.repository.CategoryRepository;
import com.expensetracker.api.repository.ExpenseRepository;
import com.expensetracker.api.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ExpenseService expenseService;

    @Test
    void createExpense_Success() {
        // setUp
        CreateExpenseRequest request = new CreateExpenseRequest();
        request.setAmount(BigDecimal.valueOf(420.5));
        request.setDescription("Pizza at 3 AM");
        request.setCategoryName("Food");
        request.setDate(LocalDate.of(2025, 11, 18));
        request.setUsername("broke_developer");

        User user = new User();
        user.setId(1L);
        user.setUsername("broke_developer");

        Category category = new Category();
        category.setId(1L);
        category.setName("Food");

        Expense savedExpense = Expense.builder()
                .id(1L)
                .amount(request.getAmount())
                .description(request.getDescription())
                .category(category)
                .date(request.getDate())
                .user(user)
                .build();

        when(userRepository.findByUsername("broke_developer")).thenReturn(Optional.of(user));
        when(categoryRepository.findByName("Food")).thenReturn(Optional.of(category));
        when(expenseRepository.save(any(Expense.class))).thenReturn(savedExpense);

        ExpenseResponse response = expenseService.createExpense(request);

        // assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(BigDecimal.valueOf(420.5), response.getAmount());
        assertEquals("Pizza at 3 AM", response.getDescription());
        assertEquals("Food", response.getCategoryName());

        verify(userRepository).findByUsername("broke_developer");
        verify(categoryRepository).findByName("Food");
        verify(expenseRepository).save(any(Expense.class));
    }

    @Test
    void createExpense_UserNotFound() {
        // setUp
        CreateExpenseRequest request = new CreateExpenseRequest();
        request.setUsername("rich_developer");
        request.setCategoryName("Luxury");

        when(userRepository.findByUsername("rich_developer")).thenReturn(Optional.empty());

        // assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> expenseService.createExpense(request)
        );

        assertTrue(exception.getMessage().contains("User not found"));
        verify(userRepository).findByUsername("rich_developer");
        verify(categoryRepository, never()).findByName(any());
        verify(expenseRepository, never()).save(any());
    }

    @Test
    void createExpense_CategoryNotFound() {
        // setUp
        CreateExpenseRequest request = new CreateExpenseRequest();
        request.setUsername("broke_developer");
        request.setCategoryName("Luxury");

        User user = new User();
        user.setUsername("broke_developer");

        when(userRepository.findByUsername("broke_developer")).thenReturn(Optional.of(user));
        when(categoryRepository.findByName("Luxury")).thenReturn(Optional.empty());

        // assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> expenseService.createExpense(request)
        );

        assertTrue(exception.getMessage().contains("Category not found"));
        verify(categoryRepository).findByName("Luxury");
        verify(expenseRepository, never()).save(any());
    }
}