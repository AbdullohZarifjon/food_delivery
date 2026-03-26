package com.example.food.util;


import com.example.food.entity.User;
import com.example.food.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

public class SecurityUtils {

    /**
     * Joriy autentifikatsiyadan o'tgan foydalanuvchini qaytaradi.
     * Agar foydalanuvchi tizimga kirmagan bo'lsa, UnauthorizedException otadi.
     */
    public static User getCurrentUser() {
        return getCurrentUserOptional()
                .orElseThrow(() -> new UnauthorizedException("Sessiya muddati tugagan yoki foydalanuvchi aniqlanmadi"));
    }

    /**
     * Joriy foydalanuvchini Optional ko'rinishida qaytaradi (null-safe).
     */
    public static Optional<User> getCurrentUserOptional() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return Optional.empty();
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof User user) {
            return Optional.of(user);
        }

        return Optional.empty();
    }

    /**
     * Joriy foydalanuvchining ID sini qaytaradi.
     */
    public static UUID getCurrentUserId() {
        return getCurrentUser().getId();
    }

}
