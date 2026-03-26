package com.example.food.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public interface CategoryDto {
    record CategoryRequest(
            @NotBlank(message = "Kategoriya nomi shart!")
            String name,

            @NotNull(message = "Tartib raqami bo'sh bo'lmasligi kerak")
            @Min(value = 0, message = "Tartib raqami 0 dan kichik bo'lishi mumkin emas")
            Integer sortOrder
    ) {}

    record UpdateCategoryRequest(
            @NotBlank(message = "Kategoriya nomi shart!")
            String name,

            @NotNull(message = "Holat (active) bo'sh bo'lmasligi kerak")
            Boolean active,

            @NotNull(message = "Tartib raqami bo'sh bo'lmasligi kerak")
            @Min(value = 0, message = "Tartib raqami 0 dan kichik bo'lishi mumkin emas")
            Integer sortOrder
    ) {}

    record CategoryResponse(
            UUID id,
            String name,
            String imageUrl,
            Integer sortOrder,
            boolean active
    ) {}
}
