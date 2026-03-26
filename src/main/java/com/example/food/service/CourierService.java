package com.example.food.service;

import com.example.food.dto.CourierDto;
import com.example.food.dto.LocationDto;
import com.example.food.dto.OrderDto;
import com.example.food.entity.Courier;
import com.example.food.entity.User;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CourierService {
    Courier createInitialInfo(User user, CourierDto.CourierRequest dto);

    Page<CourierDto.CourierResponse> getAllCouriers(Pageable pageable);

    Courier findById(UUID userId);

    void updateStatus(boolean active);

    void startDelivery();

    OrderDto.OrderResponseForMonitor acceptOrder(UUID id);

//    void handleLocationUpdate(LocationDto.CourierLocationRequest locationDto);

    void completeCourierTask(UUID orderId, LocationDto.CourierLocationRequest location);
}
