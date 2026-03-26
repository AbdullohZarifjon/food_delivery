package com.example.food.controller;

import com.example.food.dto.LocationDto;
import com.example.food.dto.OrderDto;
import com.example.food.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Order Management", description = "Mijozlar uchun buyurtmalar")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @Operation(summary = "Mijoz tomonidan yangi buyurtma yaratish")
    public ResponseEntity<OrderDto.OrderResponseForMonitor> createOrder(@Valid @RequestBody OrderDto.OrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(request));
    }

    @GetMapping("/my-active")
    @Operation(summary = "Mijoz o'zining barcha aktiv buyurtmalarini qaytadan yuklab olishi uchun")
    public ResponseEntity<List<OrderDto.OrderResponse>> getMyActiveOrders() {
        return ResponseEntity.ok(orderService.getUserActiveOrders());
    }

    @PatchMapping("/{id}/update-items")
    @Operation(summary = "Mijoz: Buyurtma tarkibini o'zgartirish (Faqat PENDING holatida)")
    public ResponseEntity<OrderDto.OrderResponseForMonitor> updateOrderItems(
            @PathVariable UUID id,
            @Valid @RequestBody List<OrderDto.OrderItemRequest> newItems) {

        OrderDto.OrderResponseForMonitor response = orderService.updateOrderItems(id, newItems);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/cancel/{id}")
    @Operation(summary = "Mijoz: Buyurtmani bekor qilish (Faqat PENDING yoki PREPARING holatida)")
    public ResponseEntity<OrderDto.OrderResponseForMonitor> cancelOrderByCustomer(
            @PathVariable UUID id,

            @RequestParam
            @NotBlank(message = "Bekor qilish sababini ko'rsating")
            @Size(min = 5, max = 500)
            String reason) {

        return ResponseEntity.ok(orderService.cancelOrderByCustomer(id, reason));
    }

    @GetMapping("/{orderId}/courier-location")
    @Operation(summary = "Mijoz uchun kuryerning joriy (oxirgi) lokatsiyasini olish")
    public ResponseEntity<LocationDto.LocationResponse> getCourierLastLocation(@PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.getCourierLastLocation(orderId));
    }
}
