package com.example.food.controller;

import com.example.food.dto.AuthDto;
import com.example.food.dto.request.RefreshTokenDto;
import com.example.food.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth Controller", description = "APIs for managing users")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Customerlar uchun kirish")
    public ResponseEntity<AuthDto.AuthResponse> login(@Valid @RequestBody AuthDto.AuthRequestForCustomers loginDto) {
        AuthDto.AuthResponse response = authService.login(loginDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sign-in/admin")
    @Operation(summary = "Faqat adminlar uchun kirish")
    public ResponseEntity<AuthDto.AuthResponse> loginForAdmins(@Valid @RequestBody AuthDto.AuthRequestForAdminsAndCouriers loginDto) {
        AuthDto.AuthResponse response = authService.loginForAdmins(loginDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sign-in/courier")
    @Operation(summary = "Faqat courierlar uchun kirish")
    public ResponseEntity<AuthDto.AuthResponse> loginForCouriers(@Valid @RequestBody AuthDto.AuthRequestForAdminsAndCouriers loginDto) {
        AuthDto.AuthResponse response = authService.loginForCouriers(loginDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token")
    public ResponseEntity<AuthDto.AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenDto refreshTokenDto) {
        AuthDto.AuthResponse response = authService.refreshToken(refreshTokenDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization", required = false) String token, @RequestBody RefreshTokenDto refreshTokenDto) {
        authService.logout(token, refreshTokenDto.refreshToken());
        return ResponseEntity.noContent().build();
    }

}
