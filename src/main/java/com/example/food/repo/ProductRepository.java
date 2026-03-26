package com.example.food.repo;

import com.example.food.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    // 1. EXISTS - Bunga JOIN shart emas, performans uchun eng yaxshisi
    boolean existsByNameAndDeletedFalse(String name);

    // 2. SINGLE FIND - Hammasiga EntityGraph qo'shilgan, N+1 muammosi yo'q
    @EntityGraph(attributePaths = {"categories", "promotion"})
    Optional<Product> findByIdAndDeletedFalse(UUID id);

    @EntityGraph(attributePaths = {"promotion"})
    Optional<Product> findByIdAndDeletedFalseAndActiveIsTrue(UUID id);

    // 3. PAGINATION (Admin & Client) - Eng og'ir so'rovlar, JOIN bilan optimallashdi
    @EntityGraph(attributePaths = {"categories", "promotion"})
    Page<Product> findAllByDeletedFalse(Pageable pageable);

    // Kategoriya bo'yicha (ManyToMany bog'lanish orqali)
    @EntityGraph(attributePaths = {"promotion"})
    @Query("SELECT p FROM Product p JOIN p.categories c " +
            "WHERE c.id = :categoryId " +
            "AND p.active = true AND p.deleted = false " +
            "ORDER BY p.name ASC")
    List<Product> findByCategoryIdCustom(UUID categoryId, Pageable pageable);

    // Nomi bo'yicha qidirish
    @EntityGraph(attributePaths = {"promotion"})
    @Query("SELECT p FROM Product p " +
            "WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) " +
            "AND p.active = true AND p.deleted = false " +
            "ORDER BY p.name ASC")
    List<Product> searchByNameCustom(String name, Pageable pageable);

    @EntityGraph(attributePaths = {"categories", "promotion"})
    Page<Product> findAllByPromotionIdAndDeletedFalse(UUID promotionId, Pageable pageable);

    // 4. BULK OPERATIONS
    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.promotion = null WHERE p.promotion.id = :promotionId")
    void detachPromotionFromProducts(@Param("promotionId") UUID promotionId);

    // 5. MULTIPLE FIND - Bunga ham EntityGraph qo'shdik, chunki bu metod
    // natijasini DTO ga o'girsak JOIN kerak bo'ladi
    @EntityGraph(attributePaths = {"categories", "promotion"})
    @Query("SELECT p FROM Product p WHERE p.id IN :ids AND p.active = true AND p.deleted = false")
    List<Product> findMassiveProducts(@Param("ids") List<UUID> ids);
}