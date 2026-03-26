package com.example.food.dto;

import com.example.food.entity.enums.MeasurementUnit;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ProductDto {
    record ProductRequest(
            @NotBlank(message = "Mahsulot nomi shart!")
            String name,

            String description,

            @NotNull(message = "Narx shart!")
            @DecimalMin(value = "0.01", message = "Narx 0 dan katta bo'lishi kerak")
            BigDecimal price,

//            @Min(value = 0, message = "Ombor qoldig'i manfiy bo'lmasin")
//            Integer stockQuantity,

            @NotNull(message = "O'lchov birligi shart!")
            MeasurementUnit unit,

            @NotEmpty(message = "Kamida bitta kategoriya tanlang!")
            List<UUID> categoryIds
    ) {}

    record ProductShortResponse(
            UUID id,
            String name,
            String description,
            String imageUrl,
            BigDecimal price,
            BigDecimal currentPrice,
            MeasurementUnit unit,
            PromotionForProductResponse promotion // Aksiya bo'lsa, narx uchun ko'rinishi shart
    ) {}

    record ProductResponse(
            UUID id,
            String name,
            String description,
            String imageUrl,
            BigDecimal price,
            BigDecimal currentPrice, // Service hisoblab beradi
//            Integer stockQuantity,
            MeasurementUnit unit,
            List<CategoryDto.CategoryResponse> categories,
            PromotionForProductResponse promotion // Response'da ko'rinishi foydali
    ) {}

    record PromotionForProductResponse(
            String title,
            Integer discountPercentage, // Necha foiz chegirma? (masalan: 20)
            BigDecimal fixedDiscountAmount, // Yoki aniq summa chegirma (masalan: 10,000 so'm)
            LocalDateTime startDate,
            LocalDateTime endDate,
            Boolean isActive
    ) {
    }
}
