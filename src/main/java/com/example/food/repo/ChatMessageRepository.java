package com.example.food.repo;

import com.example.food.entity.ChatMessage;
import com.example.food.entity.enums.MessageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    @Query("SELECT m FROM ChatMessage m WHERE " +
            "((m.senderId = :u1 AND m.recipientId = :u2) OR (m.senderId = :u2 AND m.recipientId = :u1)) " +
            "AND m.createdAt >= :since " + // Belgilangan vaqtdan keyingi xabarlar
            "ORDER BY m.createdAt DESC")
    Page<ChatMessage> findRecentChatHistory(UUID u1, UUID u2, LocalDateTime since, Pageable pageable);

    @Modifying
    @Query("UPDATE ChatMessage m SET m.status = 'READ' " +
            "WHERE m.senderId = :recipientId AND m.recipientId = :userId " +
            "AND m.status = 'SENT'")
    void markMessagesAsRead(UUID userId, UUID recipientId);
}