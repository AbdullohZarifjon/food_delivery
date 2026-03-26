package com.example.food.dto.response;


import lombok.Builder;

import java.util.List;

@Builder
public record CourierResponse(
        String name,
        String username,
        String phoneNumber,
        String status,
        List<String> roles,
        String type,
        String vehicleNumber,
        String car_model,
        boolean isOnline) {
}
