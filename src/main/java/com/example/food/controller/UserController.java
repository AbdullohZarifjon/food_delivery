package com.example.food.controller;

import com.example.food.dto.AdminDto;
import com.example.food.dto.UserDto;
import com.example.food.service.UserService;
import com.example.food.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/users")
@Tag(name = "User Controller", description = "APIs for managing users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @Operation(summary = "Joriy foydalanuvchi ma'lumotlari", description = "Tizimga kirgan foydalanuvchining shaxsiy ma'lumotlarini qaytaradi")
    public ResponseEntity<AdminDto.AdminResponse> getMyProfile() {
        return ResponseEntity.ok(userService.getUserById());
    }

}
