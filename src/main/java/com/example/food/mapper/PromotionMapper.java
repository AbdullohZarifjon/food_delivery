package com.example.food.mapper;

import com.example.food.dto.PromotionDto;
import com.example.food.entity.Product;
import com.example.food.entity.Promotion;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface PromotionMapper {

    // Yangi yaratish uchun
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "active", constant = "true") // Yangi ochilganda doim true
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    Promotion toEntity(PromotionDto.PromotionRequest request);


    // Mavjudini yangilash uchun
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "active", ignore = true) // Update-da statusga teginmaymiz
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void updateEntity(@MappingTarget Promotion promotion, PromotionDto.PromotionRequest request);

    PromotionDto.PromotionResponse toResponse(Promotion promotion);

    // Senior, mana bu metod mahsulot narxini aksiyaga qarab hisoblash uchun
    @Named("calculatePrice")
    default BigDecimal calculatePrice(Product product) {
        BigDecimal originalPrice = product.getPrice();
        Promotion promo = product.getPromotion();

        if (promo == null || Boolean.FALSE.equals(promo.getActive())) {
            return originalPrice;
        }

        LocalDateTime now = LocalDateTime.now();
        // Vaqt oralig'ini tekshirish
        if (now.isBefore(promo.getStartDate()) || now.isAfter(promo.getEndDate())) {
            return originalPrice;
        }

        // Foizli chegirma
        if (promo.getDiscountPercentage() != null) {
            BigDecimal discount = originalPrice.multiply(BigDecimal.valueOf(promo.getDiscountPercentage()))
                    .divide(BigDecimal.valueOf(100));
            return originalPrice.subtract(discount);
        }

        // Aniq summa chegirma
        if (promo.getFixedDiscountAmount() != null) {
            BigDecimal result = originalPrice.subtract(promo.getFixedDiscountAmount());
            return result.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : result;
        }

        return originalPrice;
    }
}