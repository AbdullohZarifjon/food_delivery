package com.example.food.dto;

import com.example.food.entity.enums.MeasurementUnit;
import com.example.food.entity.enums.UserRole;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface OrderDto {

    record OrderRequest(
            @NotEmpty(message = "Savat bo'sh bo'lishi mumkin emas")
            List<OrderItemRequest> items,

            @NotBlank(message = "Yetkazib berish manzili nomi shart")
            String deliveryAddressName,

            @NotNull(message = "Latitude kiritilishi shart")
            Double latitude,

            @NotNull(message = "Longitude kiritilishi shart")
            Double longitude,

            @NotBlank(message = "Bog'lanish uchun telefon raqami shart")
            @Pattern(regexp = "^\\+998\\d{9}$", message = "Telefon raqami noto'g'ri formatda")
            String contactPhone
    ) {}

    record OrderItemRequest(
            @NotNull(message = "Mahsulot IDsi shart")
            UUID productId,
            @Min(value = 1, message = "Kamida 1 dona mahsulot bo'lishi kerak")
            Integer quantity
    ) {}

    record OrderResponse(
            UUID id,
            String orderCode,
            String customerName,
            String contactPhone,
            String courierName,
            String status,

            // Narxlar va hisob-kitob
            BigDecimal itemsTotal,      // Faqat ovqatlar summasi
            BigDecimal deliveryFee,     // Yetkazib berish narxi
            BigDecimal totalPrice,      // Jami (ovqatlar + dostavka)

            // Joylashuv ma'lumotlari
            String deliveryAddressName,
            Double latitude,            // Pointdan olinadi
            Double longitude,           // Pointdan olinadi
            Double distance,            // km da

            List<OrderItemResponse> items,
            LocalDateTime createdAt,

            // Agar Status Cancelled bo'lsa
            String cancelledByName,
            UserRole cancelledByRole,
            String cancellationReason
    ) {}


    record OrderItemResponse(
            UUID productId,
            String productName,
            BigDecimal originalPriceAtOrder,
            BigDecimal priceAtOrder,
            Integer quantity,
            BigDecimal rowTotal,
            MeasurementUnit unitSnapshot
    ) {}

    record OrderResponseForMonitor(
            UUID id,
            String orderCode,
            String status,
            BigDecimal deliveryFee,     // Yetkazib berish narxi
            BigDecimal totalPrice,
            String deliveryAddressName,
            Double distance
    ) {}
}