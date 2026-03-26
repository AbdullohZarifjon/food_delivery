package com.example.food.repo;

import com.example.food.entity.Promotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PromotionRepository extends JpaRepository<Promotion, UUID> {

    // 1. Aktiv aksiyalar (Client uchun)
    @Query("SELECT p FROM Promotion p WHERE p.active = true AND p.deleted = false " +
            "AND :now BETWEEN p.startDate AND p.endDate")
    List<Promotion> findAllActivePromotions(@Param("now") LocalDateTime now);

    // 2. Oddiy find - odatda bunga EntityGraph shart emas,
    // chunki mahsulotlarni alohida Pageable bilan olyapmiz.
    Optional<Promotion> findByIdAndDeletedFalse(UUID uuid);

    // 3. Scheduler uchun - Faqat ID va Active status kerak, mahsulotlarni yuklash shart emas.
    List<Promotion> findAllByEndDateBeforeAndActiveTrueAndDeletedFalse(LocalDateTime now);

    // 4. Admin uchun Pagination (Boya Controllerda yozgan edik)
    Page<Promotion> findAllByDeletedFalse(Pageable pageable);

    // 5. Borligini tekshirish (Optimallashgan)
    boolean existsByIdAndDeletedFalse(UUID id);
}