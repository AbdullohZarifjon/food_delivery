package com.example.food.service;

import com.example.food.dto.AuthDto;
import com.example.food.dto.request.RefreshTokenDto;
import jakarta.validation.constraints.NotBlank;

public interface AuthService {

    AuthDto.AuthResponse login(AuthDto.AuthRequestForCustomers authRequestForCustomers);

    AuthDto.AuthResponse refreshToken(RefreshTokenDto refreshTokenDto);

    void logout(String token, @NotBlank(message = "Refresh token bo'sh bo'lmasligi kerak") String s);

    AuthDto.AuthResponse loginForAdmins(AuthDto.AuthRequestForAdminsAndCouriers loginDto);

    AuthDto.AuthResponse loginForCouriers(AuthDto.AuthRequestForAdminsAndCouriers loginDto);
}
