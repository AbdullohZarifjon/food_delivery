package com.example.food.entity;

import com.example.food.entity.enums.ChatType;
import com.example.food.entity.enums.MessageStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "chat_messages")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage extends BaseEntity implements Serializable {

    @Column(nullable = false)
    private UUID senderId;   // Yuboruvchi IDsi

    @Column(nullable = false)
    private UUID recipientId; // Qabul qiluvchi IDsi

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;  // Xabar matni

    @Enumerated(EnumType.STRING)
    private MessageStatus status; // SENT, DELIVERED, READ

    @Enumerated(EnumType.STRING)
    private ChatType chatType; // ADMIN_CLIENT, CLIENT_COURIER
}
