package com.example.food.service;

import com.example.food.dto.LocationDto;
import com.example.food.dto.OrderDto;
import com.example.food.entity.Courier;
import com.example.food.entity.Order;
import com.example.food.entity.User;
import com.example.food.entity.enums.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface OrderService {

    OrderDto.OrderResponseForMonitor createOrder(OrderDto.OrderRequest request);

    OrderDto.OrderResponseForMonitor confirmOrder(UUID id);

    List<OrderDto.OrderResponseForMonitor> getActiveOrders();

    OrderDto.OrderResponseForMonitor readyOrder(UUID id);

    List<OrderDto.OrderResponse> getUserActiveOrders();

    OrderDto.OrderResponseForMonitor rejectOrder(UUID id);

    List<OrderDto.OrderResponse> getCourierActiveOrders();

    OrderDto.OrderResponseForMonitor updateOrderItems(UUID id, List<OrderDto.OrderItemRequest> newItems);

    OrderDto.OrderResponseForMonitor acceptOrderByCourier(UUID orderId, Courier courier, User currentUser);

    OrderDto.OrderResponseForMonitor updateOrderItemsByAdmin(UUID id, List<OrderDto.OrderItemRequest> newItems);

    OrderDto.OrderResponseForMonitor cancelOrderByAdmin(UUID id, String reason);

    OrderDto.OrderResponse getByOrderCode(String orderCode);

    OrderDto.OrderResponseForMonitor cancelOrderByCustomer(UUID id, String reason);

    List<OrderDto.OrderResponse> getCourierOrderHistoryByDate(LocalDate date);

    void markOrdersAsOnTheWay(UUID courierId);

    void broadcastCourierLocation(LocationDto.CourierLocationRequest location);

    LocationDto.LocationResponse getCourierLastLocation(UUID orderId);

    void processOrderCompletion(UUID orderId, LocationDto.CourierLocationRequest location, UUID courierId);
}
