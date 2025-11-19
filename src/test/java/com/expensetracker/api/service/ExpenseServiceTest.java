package com.expensetracker.api.service;

import com.expensetracker.api.controller.exception.ResourceNotFoundException;
import com.expensetracker.api.dto.CreateExpenseRequest;
import com.expensetracker.api.dto.ExpenseResponse;
import com.expensetracker.api.dto.MonthlyReportResponse;
import com.expensetracker.api.dto.UpdateExpenseRequest;
import com.expensetracker.api.entity.Category;
import com.expensetracker.api.entity.Expense;
import com.expensetracker.api.entity.User;
import com.expensetracker.api.repository.CategoryRepository;
import com.expensetracker.api.repository.ExpenseRepository;
import com.expensetracker.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ExpenseService expenseService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("broke_developer");
    }

    @Test
    void createExpense_Success() {
        // setUp
        CreateExpenseRequest request = new CreateExpenseRequest();
        request.setAmount(BigDecimal.valueOf(420.5));
        request.setDescription("Pizza at 3 AM");
        request.setCategoryName("Food");
        request.setDate(LocalDate.of(2025, 11, 18));

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
        request.setCategoryName("Luxury");

        when(userRepository.findByUsername("broke_developer")).thenReturn(Optional.empty());

        // assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> expenseService.createExpense(request)
        );

        assertTrue(exception.getMessage().contains("User not found"));
        verify(userRepository).findByUsername("broke_developer");
        verify(categoryRepository, never()).findByName(any());
        verify(expenseRepository, never()).save(any());
    }

    @Test
    void createExpense_CategoryNotFound() {
        // setUp
        CreateExpenseRequest request = new CreateExpenseRequest();
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

    @Test
    void getAllExpenses_Success() {
        // setUp
        User user = new User();
        user.setId(1L);
        user.setUsername("broke_developer");

        Category category1 = new Category();
        category1.setId(1L);
        category1.setName("Food");

        Category category2 = new Category();
        category2.setId(2L);
        category2.setName("Transport");

        Expense expense1 = Expense.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(50.00))
                .description("Omelette")
                .category(category1)
                .date(LocalDate.now())
                .user(user)
                .build();

        Expense expense2 = Expense.builder()
                .id(2L)
                .amount(BigDecimal.valueOf(8.00))
                .description("Metro")
                .category(category2)
                .date(LocalDate.now())
                .user(user)
                .build();

        when(userRepository.findByUsername("broke_developer")).thenReturn(Optional.of(user));
        when(expenseRepository.findByUserId(1L)).thenReturn(List.of(expense1, expense2));

        List<ExpenseResponse> responses = expenseService.getAllExpenses();

        // assert
        assertEquals(2, responses.size());
        assertEquals("Omelette", responses.get(0).getDescription());
        assertEquals("Metro", responses.get(1).getDescription());
        verify(expenseRepository).findByUserId(1L);
    }

    @Test
    void getExpensesByCategory_Success() {
        // setUp
        User user = new User();
        user.setId(1L);
        user.setUsername("broke_developer");

        Category category = new Category();
        category.setId(1L);
        category.setName("Food");

        Expense expense = Expense.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(150.00))
                .description("Instance spicy noodles")
                .category(category)
                .date(LocalDate.now())
                .user(user)
                .build();

        when(userRepository.findByUsername("broke_developer")).thenReturn(Optional.of(user));
        when(expenseRepository.findByUserIdAndCategoryName(1L, "Food")).thenReturn(List.of(expense));

        List<ExpenseResponse> responses = expenseService.getExpensesByCategory("Food");

        // assert
        assertEquals(1, responses.size());
        assertEquals("Instance spicy noodles", responses.get(0).getDescription());
        assertEquals("Food", responses.get(0).getCategoryName());
        verify(expenseRepository).findByUserIdAndCategoryName(1L, "Food");
    }

    @Test
    void updateExpense_Success() {
        // setUp
        UpdateExpenseRequest request = new UpdateExpenseRequest();
        request.setAmount(BigDecimal.valueOf(200.00));
        request.setDescription("Energy drinks");
        request.setCategoryName("Food");
        request.setDate(LocalDate.of(2025, 11, 19));

        User user = new User();
        user.setId(1L);
        user.setUsername("broke_developer");

        Category category = new Category();
        category.setId(1L);
        category.setName("Food");

        Expense existingExpense = Expense.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(50.00))
                .description("Old description")
                .category(category)
                .date(LocalDate.now())
                .user(user)
                .build();

        Expense updatedExpense = Expense.builder()
                .id(1L)
                .amount(request.getAmount())
                .description(request.getDescription())
                .category(category)
                .date(request.getDate())
                .user(user)
                .build();

        when(userRepository.findByUsername("broke_developer")).thenReturn(Optional.of(user));
        when(expenseRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(existingExpense));
        when(categoryRepository.findByName("Food")).thenReturn(Optional.of(category));
        when(expenseRepository.save(any(Expense.class))).thenReturn(updatedExpense);

        ExpenseResponse response = expenseService.updateExpense(1L, request);

        // assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(BigDecimal.valueOf(200.00), response.getAmount());
        assertEquals("Energy drinks", response.getDescription());
        verify(expenseRepository).findByIdAndUserId(1L, 1L);
    }

    @Test
    void deleteExpense_Success() {
        // setUp
        User user = new User();
        user.setId(1L);
        user.setUsername("broke_developer");

        Category category = new Category();
        category.setId(1L);
        category.setName("Food");

        Expense expense = Expense.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(50.00))
                .description("Regrettable purchase")
                .category(category)
                .date(LocalDate.now())
                .user(user)
                .build();

        when(userRepository.findByUsername("broke_developer")).thenReturn(Optional.of(user));
        when(expenseRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(expense));

        expenseService.deleteExpense(1L);

        // assert
        verify(expenseRepository).findByIdAndUserId(1L, 1L);
        verify(expenseRepository).delete(expense);
    }

    @Test
    void getMonthlyReport_Success() {
        // setUp
        User user = new User();
        user.setId(1L);
        user.setUsername("broke_developer");

        Category coffeeCategory = new Category();
        coffeeCategory.setId(1L);
        coffeeCategory.setName("Coffee");

        Category foodCategory = new Category();
        foodCategory.setId(2L);
        foodCategory.setName("Food");

        Expense expense1 = Expense.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(150.00))
                .description("Lamiz addiction phase 1")
                .category(coffeeCategory)
                .date(LocalDate.of(2025, 11, 5))
                .user(user)
                .build();

        Expense expense2 = Expense.builder()
                .id(2L)
                .amount(BigDecimal.valueOf(80.00))
                .description("More coffee to survive deadlines")
                .category(coffeeCategory)
                .date(LocalDate.of(2025, 11, 15))
                .user(user)
                .build();

        Expense expense3 = Expense.builder()
                .id(3L)
                .amount(BigDecimal.valueOf(50.00))
                .description("Night pizza")
                .category(foodCategory)
                .date(LocalDate.of(2025, 11, 20))
                .user(user)
                .build();

        when(userRepository.findByUsername("broke_developer")).thenReturn(Optional.of(user));
        when(expenseRepository.findByUserIdAndYearAndMonth(1L, 2025, 11))
                .thenReturn(List.of(expense1, expense2, expense3));

        MonthlyReportResponse report = expenseService.getMonthlyReport(2025, 11);

        // assert
        assertNotNull(report);
        assertEquals(2025, report.getYear());
        assertEquals(11, report.getMonth());
        assertEquals(BigDecimal.valueOf(280.00), report.getTotalAmount());
        assertEquals(3, report.getExpenseCount());
        assertEquals(BigDecimal.valueOf(230.00), report.getSpendingByCategory().get("Coffee"));
        assertEquals(BigDecimal.valueOf(50.00), report.getSpendingByCategory().get("Food"));
        assertTrue(report.getAlerts().isEmpty());
        verify(expenseRepository).findByUserIdAndYearAndMonth(1L, 2025, 11);
    }

    @Test
    void getMonthlyReport_WithAlerts_ReturnsMultipleAlerts() {
        // setUp
        User user = new User();
        user.setId(1L);
        user.setUsername("broke_developer");

        Category coffeeCategory = new Category();
        coffeeCategory.setId(1L);
        coffeeCategory.setName("Coffee");

        Category foodCategory = new Category();
        foodCategory.setId(2L);
        foodCategory.setName("Food");

        Expense expense1 = Expense.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(2100.00))
                .description("Coffee overload")
                .category(coffeeCategory)
                .date(LocalDate.of(2025, 11, 5))
                .user(user)
                .build();

        Expense expense2 = Expense.builder()
                .id(2L)
                .amount(BigDecimal.valueOf(2500.00))
                .description("Too much takeout")
                .category(foodCategory)
                .date(LocalDate.of(2025, 11, 15))
                .user(user)
                .build();

        when(userRepository.findByUsername("broke_developer")).thenReturn(Optional.of(user));
        when(expenseRepository.findByUserIdAndYearAndMonth(1L, 2025, 11))
                .thenReturn(List.of(expense1, expense2));

        MonthlyReportResponse report = expenseService.getMonthlyReport(2025, 11);

        // assert
        assertEquals(2, report.getAlerts().size());
        assertTrue(report.getAlerts().stream().anyMatch(alert -> alert.contains("Coffee")));
        assertTrue(report.getAlerts().stream().anyMatch(alert -> alert.contains("Food")));
    }
}