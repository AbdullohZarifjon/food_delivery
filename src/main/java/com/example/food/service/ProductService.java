package com.example.food.service;

import com.example.food.dto.ProductDto;
import com.example.food.entity.Product;
import com.example.food.entity.Promotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ProductService {
    ProductDto.ProductResponse create(ProductDto.ProductRequest productRequest, MultipartFile file);

    ProductDto.ProductResponse update(UUID id, ProductDto.ProductRequest productRequest, MultipartFile file);

    void delete(UUID id);

    ProductDto.ProductResponse getByIdForAdmin(UUID id);

    Page<ProductDto.ProductResponse> getAllForAdmin(Pageable pageable);

    void updatePrice(UUID id, BigDecimal price);

    void changeStatus(UUID id, boolean active);

    List<ProductDto.ProductShortResponse> getProductsByCategoryId(UUID categoryId, int page);

    ProductDto.ProductShortResponse getById(UUID id);

    List<ProductDto.ProductShortResponse> searchByName(String name, int page);

    void removePromotionReference(UUID id);

    void assignPromotionToProducts(Promotion promotion, List<UUID> productIds);

    Page<ProductDto.ProductResponse> getProductsByPromotionId(UUID promotionId, Pageable pageable);

    Product findProductByIdForUser(UUID id);
}
