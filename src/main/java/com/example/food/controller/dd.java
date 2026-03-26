//package com.example.food.controller;
//
//import com.example.food.dto.request.LocationUpdate;
//import org.springframework.messaging.handler.annotation.MessageMapping;
//import org.springframework.messaging.handler.annotation.Payload;
//
//import java.time.Duration;
//
//
//public class dd {
//
//    @MessageMapping("/courier.location")
//    public void updateLocation(@Payload LocationUpdate location) {
//        // 1. Kurerning oxirgi lokatsiyasini Redis-da saqlaymiz
//        // Bu kurer "online" ekanini ham bildiradi
//        String redisKey = "courier_loc:" + location.getCourierId();
//        redisTemplate.opsForValue().set(redisKey, location, Duration.ofMinutes(5));
//
//        // 2. Bu buyurtmani kutayotgan mijozga lokatsiyani yuboramiz
//        // Manzil: /topic/order/{orderId}
//        messagingTemplate.convertAndSend(
//                "/topic/order/" + location.getOrderId(),
//                location
//        );
//    }
//}
