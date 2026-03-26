package com.example.food.service.impl;

import com.example.food.dto.ProductDto;
import com.example.food.dto.PromotionDto;
import com.example.food.entity.Promotion;
import com.example.food.exception.BadRequestException;
import com.example.food.exception.RecordNotFoundException;
import com.example.food.mapper.PromotionMapper;
import com.example.food.repo.PromotionRepository;
import com.example.food.service.ProductService;
import com.example.food.service.PromotionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;
    private final PromotionMapper promotionMapper;
    private final ProductService productService;

    public PromotionServiceImpl(PromotionRepository promotionRepository, PromotionMapper promotionMapper, ProductService productService) {
        this.promotionRepository = promotionRepository;
        this.promotionMapper = promotionMapper;
        this.productService = productService;
    }

    @Override
    @Transactional
    public PromotionDto.PromotionResponse create(PromotionDto.PromotionRequest request) {
        Promotion promotion = promotionMapper.toEntity(request);
        return promotionMapper.toResponse(promotionRepository.save(promotion));
    }

    @Override
    @Transactional
    @CacheEvict(value = {"products", "products-category"}, allEntries = true)
    public PromotionDto.PromotionResponse update(UUID id, PromotionDto.PromotionRequest request) {
        Promotion promotion = findEntityById(id);

        promotionMapper.updateEntity(promotion, request);
        return promotionMapper.toResponse(promotionRepository.save(promotion));
    }


    @Override
    @Transactional
    @CacheEvict(value = {"products", "products-category"}, allEntries = true)
    public void delete(UUID id) {
        Promotion promotion = findEntityById(id);

        productService.removePromotionReference(id);

        promotion.setDeleted(true);
        promotion.setActive(false);
        promotionRepository.save(promotion);
    }


    @Override
    public Promotion findEntityById(UUID id) {
        return promotionRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RecordNotFoundException("Aksiya topilmadi: " + id));
    }


    @Override
    @Transactional
    @CacheEvict(value = {"products", "products-category"}, allEntries = true)
    public void assignPromotionToProducts(UUID promotionId, List<UUID> productIds) {
        Promotion promotion = findEntityById(promotionId);

        if (!promotion.getActive()) {
            throw new BadRequestException("Aksiya active emas : " + promotionId);
        }
        // 2. Mahsulotlarga biriktirishni mahsulot servisiga topshiramiz
        productService.assignPromotionToProducts(promotion, productIds);
    }


    @Override
    @Transactional(readOnly = true) // Performance uchun
    public PromotionDto.PromotionResponse getById(UUID id) {
        return promotionMapper.toResponse(findEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PromotionDto.PromotionResponse> getAll(Pageable pageable) {
        // STANDART findAll ISHLATMANG! O'chirilganlarni ham olib keladi.
        return promotionRepository.findAllByDeletedFalse(pageable)
                .map(promotionMapper::toResponse);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"products", "products-category"}, allEntries = true) // Keshni tozalash SHART!
    public void changeStatus(UUID id, boolean active) {
        Promotion promotion = findEntityById(id);
        promotion.setActive(active);
        promotionRepository.save(promotion); // @Transactional borligi uchun shart emas, lekin yozsa ham xato emas
        log.info("Promotion status changed to {} for ID: {}", active, id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDto.ProductResponse> getAssignedProducts(UUID id, Pageable pageable) {
        if (!promotionRepository.existsByIdAndDeletedFalse(id)) {
            throw new RecordNotFoundException("Aksiya topilmadi: " + id);
        }
        return productService.getProductsByPromotionId(id, pageable);
    }
}
