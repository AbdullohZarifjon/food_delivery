package com.example.food.service;

import com.example.food.dto.LocationDto;
import com.example.food.dto.OrderDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
public class NotificationService {

    // Admin va Kurerlar monitori uchun (Hamma bir xil narsani ko'radi)
    private final CopyOnWriteArrayList<SseEmitter> monitorEmitters = new CopyOnWriteArrayList<>();

    // Mijozlar uchun (Har bir mijoz faqat o'z zakazini eshitadi)
    private final Map<String, SseEmitter> userEmitters = new ConcurrentHashMap<>();

    // 1. Monitor (KFC monitori) uchun ulanish
    public SseEmitter subscribeToMonitor() {
        SseEmitter emitter = new SseEmitter(0L); // Cheksiz vaqt
        monitorEmitters.add(emitter);
        emitter.onCompletion(() -> monitorEmitters.remove(emitter));
        emitter.onTimeout(() -> monitorEmitters.remove(emitter));
        return emitter;
    }

    // 2. Mijoz uchun ulanish
    public SseEmitter subscribeToUser(String username) {
        SseEmitter emitter = new SseEmitter(0L);
        userEmitters.put(username, emitter);
        emitter.onCompletion(() -> userEmitters.remove(username));
        emitter.onTimeout(() -> userEmitters.remove(username));
        return emitter;
    }

    // 3. Bizning asosiy metodimiz (OrderServiceImpl shuni chaqiradi)
    public void sendOrderNotification(OrderDto.OrderResponseForMonitor response, String username) {
        broadcastToMonitor(response);

        SseEmitter userEmitter = userEmitters.get(username);
        if (userEmitter != null) {
            try {
                userEmitter.send(SseEmitter.event()
                        .name("ORDER_STATUS_UPDATE")
                        .data(response));
            } catch (IOException e) {
                userEmitters.remove(username);
            }
        }
    }

    // 1. Koordinatalarni mijozga uzatish (Siz so'ragan yangi metod)
    public void sendLocationUpdate(String username, LocationDto.LocationResponse location) {
        SseEmitter userEmitter = userEmitters.get(username);
        if (userEmitter != null) {
            try {
                // Front-end osonroq tanib olishi uchun event name bilan yuboramiz
                userEmitter.send(SseEmitter.event()
                        .name("COURIER_LOCATION")
                        .data(location));
            } catch (IOException e) {
                log.warn("Mijozga lokatsiya yuborishda xato, emitter o'chirildi: {}", username);
                userEmitters.remove(username);
            }
        }
    }

    private void broadcastToMonitor(Object data) {
        for (SseEmitter emitter : monitorEmitters) {
            try {
                emitter.send(data);
            } catch (IOException e) {
                monitorEmitters.remove(emitter);
            }
        }
    }
}