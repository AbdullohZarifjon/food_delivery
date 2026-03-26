package com.example.food.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TypingNotification {
    private UUID senderId;    // Kim yozyapti
    private UUID recipientId; // Kimga ko'rinishi kerak
    private String senderName; // Foydalanuvchi ismi (ekranda chiqishi uchun)
    private boolean isTyping; // true bo'lsa yozyapti, false bo'lsa to'xtadi
}