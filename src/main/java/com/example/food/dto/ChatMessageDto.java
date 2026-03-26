package com.example.food.dto;

import com.example.food.entity.enums.ChatType;
import com.example.food.entity.enums.MessageStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ChatMessageDto {
    record ChatMessageRequest(
            @NotNull(message = "Sender ID bo'sh bo'lmasligi kerak")
            UUID senderId,

            @NotNull(message = "Recipient ID bo'sh bo'lmasligi kerak")
            UUID recipientId,

            @NotBlank(message = "Xabar mazmuni bo'sh bo'lishi mumkin emas")
            String content,

            @NotNull(message = "Chat turi ko'rsatilishi shart")
            ChatType chatType
    ) {
    }

    record ChatMessageResponse(
            UUID id,
            UUID senderId,
            UUID recipientId,
            String content,
            MessageStatus status,
            ChatType chatType,
            LocalDateTime createdAt
    ) {
    }
}
