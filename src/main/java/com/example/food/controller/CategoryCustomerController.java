package com.example.food.controller;

import com.example.food.dto.CategoryDto;
import com.example.food.service.CategoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories") // Hamma kirishi mumkin
@Tag(name = "Category Customer API")
public class CategoryCustomerController {
    private final CategoryService categoryService;

    public CategoryCustomerController(CategoryService categoryService) { this.categoryService = categoryService; }

    @GetMapping("/active")
    public ResponseEntity<List<CategoryDto.CategoryResponse>> getActiveCategories() {
        return ResponseEntity.ok(categoryService.getActiveCategories());
    }
}
