package com.expensetracker.api.service;

import com.expensetracker.api.controller.exception.DuplicateResourceException;
import com.expensetracker.api.dto.CategoryResponse;
import com.expensetracker.api.dto.CreateCategoryRequest;
import com.expensetracker.api.entity.Category;
import com.expensetracker.api.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryResponse createCategory(CreateCategoryRequest request) {
        if (categoryRepository.findByName(request.getName()).isPresent()) {
            throw new DuplicateResourceException("Category already exists: " + request.getName());
        }

        Category category = new Category();
        category.setName(request.getName());

        Category saved = categoryRepository.save(category);

        return CategoryResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .build();
    }
}