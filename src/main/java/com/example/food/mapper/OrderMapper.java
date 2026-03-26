package com.example.food.mapper;

import com.example.food.dto.OrderDto;
import com.example.food.entity.Order;
import com.example.food.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface OrderMapper {

    @Mapping(target = "latitude", expression = "java(order.getDeliveryAddress() != null ? order.getDeliveryAddress().getY() : null)")
    @Mapping(target = "longitude", expression = "java(order.getDeliveryAddress() != null ? order.getDeliveryAddress().getX() : null)")
    @Mapping(target = "itemsTotal", expression = "java(order.getTotalPrice().subtract(order.getDeliveryFee()))")
    OrderDto.OrderResponse toResponse(Order order);


    OrderDto.OrderResponseForMonitor toResponseForMonitor(Order order);

    // OrderItem listini map qilish uchun bu metod shart!
    OrderDto.OrderItemResponse toItemResponse(OrderItem item);
}