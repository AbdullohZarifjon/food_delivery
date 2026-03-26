package com.example.food.service.impl;

import com.example.food.dto.OrderDto;
import com.example.food.entity.Order;
import com.example.food.entity.OrderItem;
import com.example.food.entity.Product;
import com.example.food.exception.BadRequestException;
import com.example.food.service.OrderItemService;
import com.example.food.service.ProductService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderItemServiceImpl implements OrderItemService {
    private final ProductService productService;

    public OrderItemServiceImpl(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public List<OrderItem> createOrderItems(Order order, List<OrderDto.OrderItemRequest> items) {
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderDto.OrderItemRequest itemReq : items) {
            Product product = productService.findProductByIdForUser(itemReq.productId());

            // 1. Stock va Aktivlik tekshiruvi
            if (!product.isActive()) {
                throw new BadRequestException("Mahsulot yetarli emas yoki sotuvda yo'q: " + product.getName());
            }

            // 2. Narxni snapshot qilish va hisoblash
            BigDecimal priceAtOrder = product.calculateCurrentPrice();
            BigDecimal rowTotal = priceAtOrder.multiply(BigDecimal.valueOf(itemReq.quantity()));

            // 3. OrderItem obyektini qurish
            OrderItem item = OrderItem.builder()
                    .order(order)
                    .productId(product.getId())
                    .productName(product.getName())
                    .productImageUrl(product.getImageUrl())
                    .originalPriceAtOrder(product.getPrice())
                    .priceAtOrder(priceAtOrder)
                    .quantity(itemReq.quantity())
                    .unitSnapshot(product.getUnit())
                    .rowTotal(rowTotal)
                    .build();

            orderItems.add(item);

            // 4. Stockni kamaytirish (Dirty checking orqali DBga yoziladi)
//            product.setStockQuantity(product.getStockQuantity() - itemReq.quantity());
        }
        return orderItems;
    }


}
