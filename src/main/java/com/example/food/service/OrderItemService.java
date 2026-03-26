package com.example.food.service;

import com.example.food.dto.OrderDto;
import com.example.food.entity.Order;
import com.example.food.entity.OrderItem;

import java.util.List;
import java.util.UUID;

public interface OrderItemService {
    List<OrderItem> createOrderItems(Order order, List<OrderDto.OrderItemRequest> items);

}
