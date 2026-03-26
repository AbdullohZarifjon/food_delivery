package com.example.food.controller;

import com.example.food.service.NotificationService;
import com.example.food.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/orders/stream")
@Tag(name = "Order SSE", description = "Real-time yangilanishlarni olish")
public class OrderStreamController {

    private final NotificationService notificationService;

    public OrderStreamController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping(value = "/monitor", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Admin va Kurerlar uchun monitor")
    @PreAuthorize("hasAnyRole('ADMIN', 'COURIER')")
    public SseEmitter streamMonitor() {
        return notificationService.subscribeToMonitor();
    }

    @GetMapping(value = "/me", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Mijoz o'z zakazlarini kuzatishi uchun")
    public SseEmitter streamMe() {
        String username = SecurityUtils.getCurrentUser().getUsername();
        return notificationService.subscribeToUser(username);
    }


}