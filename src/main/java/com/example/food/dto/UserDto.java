package com.example.food.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

import java.util.UUID;

@Schema(description = "Foydalanuvchi ma'lumotlari uchun DTO lari")
public interface UserDto {

    @Builder
    @Schema(name = "UserResponse", description = "Foydalanuvchi ma'lumotlarini qaytarish uchun model")
    record UserResponse(
            UUID id,

            String name,

            String phoneNumber
    ) {
    }

    @Builder
    @Schema(name = "UserUpdateRequest", description = "Profilni tahrirlash uchun model")
    record UserUpdateRequest(
            @NotBlank(message = "Ism bo'sh bo'lishi mumkin emas")
            String name,

            @Pattern(regexp = "^\\+998\\d{9}$", message = "Telefon raqami noto'g'ri formatda")
            String phoneNumber,

            String avatarUrl
    ) {
    }
}