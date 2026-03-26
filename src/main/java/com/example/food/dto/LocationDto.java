package com.example.food.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDateTime;

@Schema(description = "Geolokatsiya ma'lumotlari uchun DTO konteyneri")
public interface LocationDto {

    /**
     * Kuryerdan har 5-10 soniyada keladigan lokatsiya ma'lumotlari
     */
    @Builder
    record CourierLocationRequest(
            @NotNull(message = "Latitude bo'sh bo'lishi mumkin emas")
            @Min(value = -90, message = "Latitude -90 dan kichik bo'lishi mumkin emas")
            @Max(value = 90, message = "Latitude 90 dan katta bo'lishi mumkin emas")
            @Schema(example = "41.311081", description = "Geografik kenglik")
            Double latitude,

            @NotNull(message = "Longitude bo'sh bo'lishi mumkin emas")
            @Min(value = -180, message = "Longitude -180 dan kichik bo'lishi mumkin emas")
            @Max(value = 180, message = "Longitude 180 dan katta bo'lishi mumkin emas")
            @Schema(example = "69.240562", description = "Geografik uzunlik")
            Double longitude,

            @Schema(example = "120.5", description = "Kuryerning harakat yo'nalishi (gradusda)")
            Double bearing,

            @Schema(example = "15.5", description = "Kuryerning harakat tezligi (km/soat)")
            Double speed
    ) {}

    /**
     * SSE orqali mijozga (User) yuboriladigan lokatsiya ma'lumotlari
     */
    @Builder
    record LocationResponse(
            @Schema(example = "41.311081")
            Double latitude,

            @Schema(example = "69.240562")
            Double longitude,

            @Schema(example = "120.5", description = "Harakat yo'nalishi")
            Double bearing,

            @Schema(description = "Ma'lumot yangilangan vaqt")
            LocalDateTime updatedAt
    ) {}
}
