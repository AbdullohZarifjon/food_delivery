package com.example.food.service;

import com.example.food.dto.ProductDto;
import com.example.food.dto.PromotionDto;
import com.example.food.entity.Promotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface PromotionService {

    PromotionDto.PromotionResponse create(PromotionDto.PromotionRequest request);

    PromotionDto.PromotionResponse update(UUID id, PromotionDto.PromotionRequest request);

    void delete(UUID id);

    Promotion findEntityById(UUID id); // Ichki foydalanish uchun

    void assignPromotionToProducts(UUID promotionId, List<UUID> productIds);

    PromotionDto.PromotionResponse getById(UUID id);

    Page<PromotionDto.PromotionResponse> getAll(Pageable pageable);

    void changeStatus(UUID id, boolean active);

    Page<ProductDto.ProductResponse> getAssignedProducts(UUID id, Pageable pageable);
}
