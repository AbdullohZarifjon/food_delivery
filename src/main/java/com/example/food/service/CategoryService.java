package com.example.food.service;

import com.example.food.dto.CategoryDto;
import com.example.food.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface CategoryService {
    CategoryDto.CategoryResponse create(CategoryDto.CategoryRequest categoryRequest, MultipartFile file);

    CategoryDto.CategoryResponse update(UUID id, CategoryDto.UpdateCategoryRequest request, MultipartFile file);

    void delete(UUID id);

    CategoryDto.CategoryResponse getById(UUID id);

    Page<CategoryDto.CategoryResponse> getAllForAdmin(Pageable pageable);

    List<CategoryDto.CategoryResponse> getActiveCategories(); // Customer uchun (Kesh bilan)

    List<Category> getCategoriesByIdInAndIsDeletedFalse(List<UUID> ids);
}
