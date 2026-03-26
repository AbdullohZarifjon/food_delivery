package com.example.food.service.impl;

import com.example.food.dto.ChatMessageDto;
import com.example.food.dto.request.TypingNotification;
import com.example.food.entity.ChatMessage;
import com.example.food.entity.enums.ChatType;
import com.example.food.entity.enums.MessageStatus;
import com.example.food.exception.AccessDeniedException;
import com.example.food.mapper.ChatMapper;
import com.example.food.repo.ChatMessageRepository;
import com.example.food.repo.OrderRepository;
import com.example.food.service.ChatService;
import com.example.food.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final OrderRepository orderRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMapper chatMapper;

    @Override
    @Transactional // Tranzaksiya faqat DB amallari uchun
    public void sendMessage(ChatMessageDto.ChatMessageRequest request) {
        log.info("Processing message: {} -> {}", request.senderId(), request.recipientId());

        // 1. Validatsiya (Buni tranzaksiyadan oldin qilish ham mumkin)
        if (request.chatType() == ChatType.CLIENT_COURIER || request.chatType() == ChatType.COURIER_CLIENT) {
            if (!orderRepository.existsActiveDelivery(request.senderId(), request.recipientId())) {
                sendErrorMessage(request.senderId());
                throw new AccessDeniedException("Faqat faol buyurtma vaqtida yozisha olasiz.");
            }
        }

        // 2. DB Amallari
        ChatMessage chatMessage = chatMapper.toEntity(request);
        chatMessage.setStatus(MessageStatus.SENT);
        ChatMessage savedEntity = chatMessageRepository.save(chatMessage);

        ChatMessageDto.ChatMessageResponse response = chatMapper.toResponse(savedEntity);

        // 3. SENIOR TOUCH: Transactional After Commit
        // Xabar faqat DB-ga 100% yozilgandan keyingina WebSocket-ga chiqishi kerak!
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                // Adresatga yuborish
                messagingTemplate.convertAndSendToUser(
                        response.recipientId().toString(),
                        "/queue/messages",
                        response
                );
                // Jo'natuvchiga "OK" statusi (Acknowledge)
                messagingTemplate.convertAndSendToUser(
                        response.senderId().toString(),
                        "/queue/messages",
                        response
                );
            }
        });
    }

    private void sendErrorMessage(UUID userId) {
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/errors",
                "Xabar yuborish taqiqlangan: Faol buyurtma topilmadi."
        );
    }

    @Override
    public void sendTypingNotification(TypingNotification notification) {
        // Typing notification DB-ga yozilmaydi, shuning uchun Transaction shart emas
        messagingTemplate.convertAndSendToUser(
                notification.getRecipientId().toString(),
                "/queue/typing",
                notification
        );
    }

    @Override
    @Transactional
    public Page<ChatMessageDto.ChatMessageResponse> getTodaysHistory(UUID recipientId, Pageable pageable) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();

        // 1. 24 soatlik vaqtni hisoblash
        LocalDateTime since = LocalDateTime.now().minusHours(24);

        // 2. FAQAT BIRINCHI SAHIFA UCHUN READ QILISH
        // userId - bu siz (qabul qiluvchi), recipientId - bu yuborgan odam
        if (pageable.getPageNumber() == 0) {
            chatMessageRepository.markMessagesAsRead(currentUserId, recipientId);
        }

        // 3. Bazadan suhbatni olish
        return chatMessageRepository.findRecentChatHistory(currentUserId, recipientId, since, pageable)
                .map(chatMapper::toResponse);
    }
}
