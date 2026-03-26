package com.example.food.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourierLocationService {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String COURIER_KEY = "courier:location:";
    private static final String COURIER_GEO_KEY = "courier_locations";

    public void updateLocation(UUID courierId, double lat, double lon) {
        String key = COURIER_KEY + courierId.toString();
        Map<String, Double> coords = Map.of("lat", lat, "lon", lon);

        // Redis'da kurer joylashuvini 1 daqiqa saqlaymiz (agar signal uzilsa, u oflayn hisoblanadi)
        redisTemplate.opsForValue().set(key, coords, Duration.ofMinutes(1));
    }

    public Map<Object, Object> getLocation(UUID courierId) {
        return (Map<Object, Object>) redisTemplate.opsForValue().get(COURIER_KEY + courierId.toString());
    }


    // 1. Kurer joylashuvini yangilash (Flutter appdan har 10 sekundda keladi)
    public void updateCourierLocation(UUID courierId, double lat, double lon) {
        redisTemplate.opsForGeo().add(
                COURIER_GEO_KEY,
                new Point(lon, lat), // Diqqat: Birinchi Longitude (uzunlik) keyin Latitude (kenglik)
                courierId.toString()
        );
    }

    // 2. Mijoz atrofidagi kurerlarni topish (masalan, 5 km radiusda)
    public List<String> findNearbyCouriers(double lat, double lon, double radiusKm) {
        Circle area = new Circle(new Point(lon, lat), new Distance(radiusKm, Metrics.KILOMETERS));

        GeoResults<RedisGeoCommands.GeoLocation<Object>> results =
                redisTemplate.opsForGeo().search(COURIER_GEO_KEY, area);

        return results.getContent().stream()
                .map(res -> res.getContent().getName().toString())
                .toList();
    }
}
