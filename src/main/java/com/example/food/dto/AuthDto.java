package com.example.food.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public interface AuthDto {

    record AuthRequestForCustomers(
            @NotBlank(message = "Name cannot be blank")
            @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
            String name,

            @NotBlank(message = "Phone number cannot be blank")
            @Pattern(regexp = "^\\+998\\d{9}$", message = "Telefon raqami +998XXXXXXXXX formatida bo'lishi kerak")
            String phoneNumber) {
    }

    record AuthRequestForAdminsAndCouriers(
            @NotBlank(message = "username cannot be blank")
            @Size(min = 2, max = 50, message = "username must be between 2 and 50 characters")
            String username,

            @NotBlank(message = "Password cannot be blank")
            @Size(min = 6, max = 32, message = "password 6 tadan kam bo'lmasligi kerak!")
            String password) {
    }

    record AuthResponse(
            String accessToken,
            String refreshToken
    ) {
    }
}
