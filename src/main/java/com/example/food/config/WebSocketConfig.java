package com.example.food.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtChannelInterceptor jwtChannelInterceptor;

    public WebSocketConfig(JwtChannelInterceptor jwtChannelInterceptor) {
        this.jwtChannelInterceptor = jwtChannelInterceptor;
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Interceptor-ni kanalga ulaymiz
        registration.interceptors(jwtChannelInterceptor);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Faqat bitta chat uchun endpoint yetarli
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 1. /topic - guruhli chatlar uchun (masalan, kurer + restoran + admin bitta chatda)
        // 2. /queue - shaxsiy (1-to-1) chatlar uchun
        registry.enableSimpleBroker("/topic", "/queue");

        // Klientdan serverga xabar keladigan prefix
        registry.setApplicationDestinationPrefixes("/app");

        // Shaxsiy xabarlar uchun user prefixi (Mijozga kurerdan keladigan xabar uchun)
        registry.setUserDestinationPrefix("/user");
    }
}