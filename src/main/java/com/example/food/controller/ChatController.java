package com.example.food.controller;

import com.example.food.dto.ChatMessageDto;
import com.example.food.dto.request.TypingNotification;
import com.example.food.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/chat") // HTTP so'rovlar uchun prefix
@Tag(name = "Chat Controller", description = "WebSocket va HTTP orqali muloqotni boshqarish")
public class ChatController {

    private final ChatService chatService;

    // --- WEBSOCKET OPERATIONS ---

    @MessageMapping("/chat.sendMessage")
    @Operation(summary = "Xabar yuborish (WebSocket)", description = "STOMP orqali xabar yuborish uchun endpoint")
    public void processMessage(@Payload @Valid ChatMessageDto.ChatMessageRequest request) {
        chatService.sendMessage(request);
    }

    @MessageMapping("/chat.typing")
    @Operation(summary = "Yozayotganlik haqida bildirishnoma", description = "User yozayotganini boshqa tarafga bildirish")
    public void userTyping(@Payload @Valid TypingNotification notification) {
        chatService.sendTypingNotification(notification);
    }

    // --- HTTP OPERATIONS ---

    @GetMapping("/history/recent/{recipientId}")
    @ResponseBody
    @PreAuthorize("hasAnyRole('CLIENT', 'COURIER', 'ADMIN')")
    @Operation(summary = "Oxirgi 24 soatlik xabarlar", description = "Suhbatdosh bilan oxirgi 24 soat ichidagi gaplashilgan xabarlarni sahifalab olish")
    public Page<ChatMessageDto.ChatMessageResponse> getRecentHistory(
            @PathVariable UUID recipientId,
            @ParameterObject Pageable pageable) {

        return chatService.getTodaysHistory(recipientId, pageable);
    }
}