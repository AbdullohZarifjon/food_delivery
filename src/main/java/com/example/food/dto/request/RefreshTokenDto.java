package com.example.food.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenDto(
        @NotBlank(message = "Refresh token bo'sh bo'lmasligi kerak")
        String refreshToken
) {}