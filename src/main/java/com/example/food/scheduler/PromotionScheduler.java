package com.example.food.scheduler;

import com.example.food.entity.Promotion;
import com.example.food.repo.ProductRepository;
import com.example.food.repo.PromotionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
public class PromotionScheduler {

    private final PromotionRepository promotionRepository;
    private final ProductRepository productRepository;
    private final CacheManager cacheManager;

    // Har kuni soat 00:00 da ishga tushadi
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void processExpiredPromotions() {
        LocalDateTime now = LocalDateTime.now();
        log.info("PromotionScheduler boshlandi: {}", now);

        // 1. Muddati o'tgan, lekin hali ham active bo'lgan aksiyalarni topamiz
        List<Promotion> expiredPromotions = promotionRepository
                .findAllByEndDateBeforeAndActiveTrueAndDeletedFalse(now);

        if (expiredPromotions.isEmpty()) {
            log.info("Muddati o'tgan aksiyalar topilmadi.");
            return;
        }

        log.info("{} ta muddati o'tgan aksiya aniqlandi. Tozalash boshlanmoqda...", expiredPromotions.size());

        for (Promotion promotion : expiredPromotions) {
            // 2. Mahsulotlar bilan bog'liqlikni uzamiz (Boya yozgan bulk update metodimiz)
            productRepository.detachPromotionFromProducts(promotion.getId());

            // 3. Aksiyani nofaol holatga keltiramiz
            promotion.setActive(false);
        }

        // 4. O'zgarishlarni saqlaymiz
        promotionRepository.saveAll(expiredPromotions);

        // 5. Keshni tozalaymiz (chunki mahsulot narxlari o'zgaradi)
        clearCaches();

        log.info("PromotionScheduler muvaffaqiyatli yakunlandi.");
    }

    private void clearCaches() {
        try {
            Objects.requireNonNull(cacheManager.getCache("products")).clear();
            Objects.requireNonNull(cacheManager.getCache("products-category")).clear();
            log.info("Aksiya keshni tozalash muvaffaqiyatli yakunlandi.");
        } catch (Exception e) {
            log.warn("Keshni tozalashda xatolik: {}", e.getMessage());
        }
    }
}