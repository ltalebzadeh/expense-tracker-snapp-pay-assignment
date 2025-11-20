package com.expensetracker.api.service;

import com.expensetracker.api.controller.exception.DuplicateResourceException;
import com.expensetracker.api.dto.CategoryResponse;
import com.expensetracker.api.dto.CreateCategoryRequest;
import com.expensetracker.api.entity.Category;
import com.expensetracker.api.entity.User;
import com.expensetracker.api.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private CategoryService categoryService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("broke_developer");
        when(userService.getAuthenticatedUser()).thenReturn(user);
    }

    @Test
    void createCategory_Success() {
        // setUp
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("Junk food");

        Category savedCategory = new Category();
        savedCategory.setId(1L);
        savedCategory.setName("Junk food");

        when(userService.getAuthenticatedUser()).thenReturn(user);
        when(categoryRepository.findByName("Junk food")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

        CategoryResponse response = categoryService.createCategory(request);

        // assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Junk food", response.getName());
        verify(userService).getAuthenticatedUser();
        verify(categoryRepository).findByName("Junk food");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategory_DuplicateName_ThrowsException() {
        // setUp
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("Food");

        Category existingCategory = new Category();
        existingCategory.setId(1L);
        existingCategory.setName("Food");

        when(categoryRepository.findByName("Food")).thenReturn(Optional.of(existingCategory));

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> categoryService.createCategory(request)
        );

        // assert
        assertTrue(exception.getMessage().contains("Category already exists"));
        verify(categoryRepository).findByName("Food");
        verify(categoryRepository, never()).save(any());
    }
}