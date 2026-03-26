package com.example.food.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory();
    }

    /**
     * Senior, bu yerdagi ObjectMapper barcha LocalDateTime va Java obyektlarini
     * Redisga to'g'ri JSON formatda yozish uchun xizmat qiladi.
     */
    // 1. Umumiy va xavfsiz serializer yaratish
    private GenericJackson2JsonRedisSerializer createSerializer() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        // NON_FINAL - barcha DTO va Entity'lar uchun tip ma'lumotini qo'shadi
//        mapper.activateDefaultTyping(
//                LaissezFaireSubTypeValidator.instance,
//                ObjectMapper.DefaultTyping.NON_FINAL,
//                JsonTypeInfo.As.PROPERTY);

        return new GenericJackson2JsonRedisSerializer(mapper);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Keylar doim String
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer = createSerializer();

        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1)) // Default: 1 soat
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(createSerializer()));
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 1. Umumiy sozlamalar
        RedisCacheConfiguration defaultConfig = cacheConfiguration();

        // 2. Maxsus keshlar uchun muddatlarni belgilaymiz (Siz xohlagan qism)
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();

        // Kuryer aktiv mijozlari ro'yxati aynan 10 minut yashasin
        cacheConfigs.put("courier_active_clients", defaultConfig.entryTtl(Duration.ofMinutes(10)));

        // Kuryerning oxirgi lokatsiyasi (Adminlar uchun) 30 minut
        cacheConfigs.put("courier_last_location", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // Mahsulotlar uchun 1 kun
        cacheConfigs.put("products", defaultConfig.entryTtl(Duration.ofDays(1)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig) // Boshqa hamma keshlar uchun 1 soat
                .withInitialCacheConfigurations(cacheConfigs) // Maxsus sozlamalarni yuklaymiz
                .build();
    }
}