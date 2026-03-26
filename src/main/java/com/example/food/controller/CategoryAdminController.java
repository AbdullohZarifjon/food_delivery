package com.example.food.controller;

import com.example.food.dto.CategoryDto;
import com.example.food.exception.BadRequestException;
import com.example.food.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/categories")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Tag(name = "Category Admin API")
@Validated
public class CategoryAdminController {
    private final CategoryService categoryService;

    public CategoryAdminController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Admin: Categorylarni qo'shadi")
    public ResponseEntity<CategoryDto.CategoryResponse> create(
            @RequestPart("data") @Valid CategoryDto.CategoryRequest categoryRequest,
            @RequestPart("file") MultipartFile file) {
        // Spring o'zi file borligini tekshiradi,
        // lekin qo'shimcha xavfsizlik uchun empty-likka tekshirib yuboramiz
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Kategoriya rasmi yuklanishi shart!");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(categoryRequest, file));
    }


    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Admin: Categorylarni o'zgartiradi")
    public ResponseEntity<CategoryDto.CategoryResponse> update(
            @PathVariable UUID id,
            @RequestPart("data") @Valid CategoryDto.UpdateCategoryRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        return ResponseEntity.ok(categoryService.update(id, request, file));
    }


    @GetMapping("/{id}")
    @Operation(summary = "Admin: Categoryni qaytaradi id si orqalik")
    public ResponseEntity<CategoryDto.CategoryResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(categoryService.getById(id));
    }


    @GetMapping
    @Operation(summary = "Admin: Categorylarni sahifalangan holda qaytaradi")
    public ResponseEntity<Page<CategoryDto.CategoryResponse>> getAll(
            @PageableDefault(
                    size = 20,
                    sort = "sortOrder",
                    direction = Sort.Direction.ASC
            ) Pageable pageable) {

        // Senior-level check: Agar kimgadir juda ko'p ma'lumot kerak bo'lsa ham,
        // biz uni majburan cheklaymiz (Xavfsizlik kamari)
        if (pageable.getPageSize() > 100) {
            throw new BadRequestException("Sahifa hajmi 100 tadan oshmasligi kerak!");
        }

        return ResponseEntity.ok(categoryService.getAllForAdmin(pageable));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Admin: Categorylarni o'chiradi")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}