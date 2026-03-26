package com.example.food.controller;


import com.example.food.dto.LocationDto;
import com.example.food.dto.OrderDto;
import com.example.food.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/courier/orders")
@Tag(name = "Courier Order Management", description = "Kurerlar uchun buyurtmalar bilan ishlash")
@PreAuthorize("hasRole('COURIER')") // Butun controller darajasida kurerlik huquqini tekshiramiz
public class OrderCourierController {

    private final OrderService orderService;

    public OrderCourierController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/active-market")
    @Operation(summary = "Kurerlar uchun bozordagi barcha faol (PREPARING, READY) buyurtmalar ro'yxati")
    public ResponseEntity<List<OrderDto.OrderResponseForMonitor>> getActiveOrders() {
        return ResponseEntity.ok(orderService.getActiveOrders());
    }


    @PatchMapping("/{id}/reject")
    @Operation(summary = "Kurer: Biriktirilgan buyurtmadan voz kechish (Status -> READY_TO_PICKUP)")
    public ResponseEntity<OrderDto.OrderResponseForMonitor> rejectOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.rejectOrder(id));
    }

    @GetMapping("/my-active")
    @Operation(summary = "Kurer: O'ziga biriktirilgan faol buyurtmalarni ko'rish (ACCEPTED, DELIVERING)")
    public ResponseEntity<List<OrderDto.OrderResponse>> getMyActiveOrders() {
        return ResponseEntity.ok(orderService.getCourierActiveOrders());
    }

    @GetMapping("/my-history")
    @Operation(summary = "Kurer: Kunlik yetkazib berilgan buyurtmalar tarixi")
    public ResponseEntity<List<OrderDto.OrderResponse>> getMyOrderHistory(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        // Agar date null bo'lsa, Service ichida bugungi kun olinadi
        return ResponseEntity.ok(orderService.getCourierOrderHistoryByDate(date));
    }

    @PostMapping("/location")
    public ResponseEntity<Void> updateLocation(@RequestBody LocationDto.CourierLocationRequest locationDto) {

        orderService.broadcastCourierLocation(locationDto);

        return ResponseEntity.ok().build();
    }

}