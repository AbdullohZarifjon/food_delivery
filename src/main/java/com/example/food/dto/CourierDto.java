package com.example.food.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public interface CourierDto {
    record CourierRequest(
            @NotBlank(message = "Ism bo'sh bo'lishi mumkin emas")
            @Size(max = 50, message = "Ism 50 ta belgidan oshmasligi kerak")
            String name,

            @NotBlank(message = "Username bo'sh bo'lishi mumkin emas")
            @Size(min = 4, max = 20, message = "Username 4 va 20 belgi oralig'ida bo'lishi kerak")
            @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username faqat harf, raqam va ._- belgilaridan iborat bo'lishi mumkin")
            String username,

            @NotBlank(message = "Telefon raqami bo'sh bo'lishi mumkin emas")
            @Pattern(regexp = "^\\+998\\d{9}$", message = "Telefon raqami +998XXXXXXXXX formatida bo'lishi kerak")
            String phoneNumber,

            @NotBlank(message = "Parol bo'sh bo'lishi mumkin emas")
            @Size(min = 6, max = 32, message = "Parol uzunligi 6 va 32 oralig'ida bo'lishi kerak")
            String password,

            String courierType,

            String vehicleNumber,

            String carModel
    ) {}

    record CourierResponse(
            UUID id,
            String name,
            String username,
            String phoneNumber,
            String status,
            List<String> roles,
            String courierType,
            String vehicleNumber,
            String carModel
    ) {}
}
