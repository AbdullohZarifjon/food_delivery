package com.example.food.controller;

import com.example.food.dto.LocationDto;
import com.example.food.dto.OrderDto;
import com.example.food.service.CourierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/couriers")
@Tag(name = "For Couriers", description = "Courierlar uchun apilar")
@PreAuthorize("hasAuthority('ROLE_COURIER')")
public class CourierController {

    private final CourierService courierService;

    public CourierController(CourierService courierService) {
        this.courierService = courierService;
    }

    @PostMapping("/status")
    @Operation(summary = "Ishni boshlash yoki to'xtatish",
            description = "active=true -> ONLINE, active=false -> OFFLINE")
    public ResponseEntity<Void> updateStatus(@RequestParam boolean active) {
        courierService.updateStatus(active);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/start-delivery")
    @Operation(summary = "Kuryer yo'lga chiqqanini tasdiqlaydi")
    public ResponseEntity<Void> startDelivery() {
        courierService.startDelivery();
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/accept")
    @Operation(summary = "Kurer: Buyurtmani o'ziga biriktirish (Status -> ACCEPTED)")
    public ResponseEntity<OrderDto.OrderResponseForMonitor> acceptOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(courierService.acceptOrder(id));
    }

    @PostMapping("/{orderId}/complete")
    @Operation(summary = "Buyurtmani yakunlash (Faqat mijoz yaqinida bo'lganda)")
    public ResponseEntity<String> completeOrder(
            @PathVariable UUID orderId,
            @RequestBody @Valid LocationDto.CourierLocationRequest location) {

        courierService.completeCourierTask(orderId, location);
        return ResponseEntity.ok("Buyurtma muvaffaqiyatli topshirildi. Rahmat!");
    }




}
