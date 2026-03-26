package com.example.food.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.UUID;

public interface BannerDto {
    record BannerRequest(
            @NotBlank(message = "Sarlavha bo'sh bo'lishi mumkin emas")
            @Size(max = 100, message = "Sarlavha 100 ta belgidan oshmasligi kerak")
            String title,

            @NotNull(message = "Tartib raqami kiritilishi shart")
            @Min(value = 0, message = "Tartib raqami manfiy bo'lishi mumkin emas")
            Integer sortOrder,

            @NotNull(message = "Amal qilish muddati kiritilishi shart")
            @Future(message = "Muddati tugash vaqti faqat kelajakda bo'lishi kerak")
            LocalDateTime expiresAt
    ) {}

    // Tahrirlash uchun (Title yo'q, lekin isActive bor)
    record UpdateBannerRequest(
            @NotNull(message = "Tartib raqami kiritilishi shart")
            @Min(value = 0, message = "Tartib raqami manfiy bo'lishi mumkin emas")
            Integer sortOrder,

            @NotNull(message = "Amal qilish muddati kiritilishi shart")
            @Future(message = "Muddati tugash vaqti faqat kelajakda bo'lishi kerak")
            LocalDateTime expiresAt,

            @NotNull(message = "active true yoki false bo'lishi kerak")
            Boolean isActive
    ) {}

    record BannerResponse(
            UUID id,
            String title,
            String imageUrl,
            Integer sortOrder,
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
            LocalDateTime expiresAt,
            boolean isActive
    ) {}
}
