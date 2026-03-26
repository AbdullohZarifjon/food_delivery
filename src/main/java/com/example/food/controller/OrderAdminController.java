package com.example.food.controller;

import com.example.food.dto.OrderDto;
import com.example.food.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/orders")
@Tag(name = "Admin Order Management", description = "Adminlar uchun buyurtmalarni boshqarish")
@PreAuthorize("hasRole('ADMIN')")
@Validated
public class OrderAdminController {

    private final OrderService orderService;

    public OrderAdminController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/active-market")
    @Operation(summary = "Adminlar uchun bozordagi barcha faol (PREPARING, READY) buyurtmalar ro'yxati")
    public ResponseEntity<List<OrderDto.OrderResponseForMonitor>> getActiveOrders() {
        return ResponseEntity.ok(orderService.getActiveOrders());
    }

    @PatchMapping("/{id}/confirm")
    @Operation(summary = "Admin: Buyurtmani tasdiqlash (Status -> PREPARING)")
    public ResponseEntity<OrderDto.OrderResponseForMonitor> confirmOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.confirmOrder(id));
    }

    @PatchMapping("/{id}/ready")
    @Operation(summary = "Admin: Buyurtma tayyor bo'ldi (Status -> READY_TO_PICKUP)")
    public ResponseEntity<OrderDto.OrderResponseForMonitor> readyOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.readyOrder(id));
    }

    @PatchMapping("/{id}/update-items-by-admin")
    @Operation(summary = "Admin: Mijoz buyurtmasini tahrirlash (Har qanday holatda, mas'uliyat bilan)")
    public ResponseEntity<OrderDto.OrderResponseForMonitor> updateOrderItemsByAdmin(
            @PathVariable UUID id,
            @Valid @RequestBody List<OrderDto.OrderItemRequest> newItems) {

        // Admin uchun alohida service metodini chaqiramiz
        OrderDto.OrderResponseForMonitor response = orderService.updateOrderItemsByAdmin(id, newItems);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/cancel/{id}")
    @Operation(summary = "Admin: Buyurtmani bekor qiladi va sababni keltiradi")
    public ResponseEntity<OrderDto.OrderResponseForMonitor> cancelOrderByCode(
            @PathVariable UUID id,

            @RequestParam
            @NotBlank(message = "Bekor qilish sababi bo'sh bo'lishi mumkin emas")
            @Size(min = 5, max = 500, message = "Sabab kamida 5 ta va ko'pi bilan 500 ta belgidan iborat bo'lishi kerak")
            String reason) {

        return ResponseEntity.ok(orderService.cancelOrderByAdmin(id, reason));
    }

    @GetMapping("/search/{orderCode}")
    @Operation(summary = "Admin: Buyurtmani kodi orqali topish")
    public ResponseEntity<OrderDto.OrderResponse> getByOrderCode(
            @PathVariable
            @Pattern(regexp = "^[0-9]{5}$", message = "Buyurtma kodi 5 ta raqam bo'lishi shart")
            String orderCode) {

        return ResponseEntity.ok(orderService.getByOrderCode(orderCode));
    }
}
