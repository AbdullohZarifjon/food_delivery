package com.example.food.repo;

import com.example.food.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BannerRepository extends JpaRepository<Banner, UUID> {
    // 1-yo'l: Spring Data JPA metod nomi orqali (Query Method)
    List<Banner> findAllByActiveTrue();

    List<Banner> findAllByDeletedFalse();

    // 2-yo'l: Agar murakkabroq mantiq kerak bo'lsa (Masalan: faqat muddati o'tmaganlarni olish)
//    @Query("SELECT b FROM Banner b WHERE b.isActive = true")
//    List<Banner> findAllActiveBanners();
}