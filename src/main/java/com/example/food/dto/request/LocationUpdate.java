package com.example.food.dto.request;

import java.util.UUID;

public record LocationUpdate(
        UUID courierId,
        UUID orderId, // Qaysi buyurtma uchun harakatlanayotgani
        double lat,
        double lon) {
}
