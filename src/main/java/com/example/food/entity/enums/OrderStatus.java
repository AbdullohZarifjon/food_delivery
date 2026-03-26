package com.example.food.entity.enums;

public enum OrderStatus {
    PENDING,    // To'lov kutilmoqda yoki qabul qilinishi kutilmoqda
    PREPARING,  // Tayyorlanmoqda
    COURIER_ASSIGNED, // Kurer tayinlandi
    READY_TO_PICKUP,  // OLISHGA_TAYOR
    ACCEPTED,   // Courier qabul qildi
    ON_THE_WAY, // Yo'lda
    DELIVERED,  // Yetkazildi
    CANCELLED   // Bekor qilindi
}
