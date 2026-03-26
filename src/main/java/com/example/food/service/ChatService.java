package com.example.food.service;

import com.example.food.dto.ChatMessageDto;
import com.example.food.dto.request.TypingNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ChatService {
    void sendMessage(ChatMessageDto.ChatMessageRequest chatMessage);

    void sendTypingNotification(TypingNotification notification);

    Page<ChatMessageDto.ChatMessageResponse> getTodaysHistory(UUID recipientId, Pageable pageable);
}