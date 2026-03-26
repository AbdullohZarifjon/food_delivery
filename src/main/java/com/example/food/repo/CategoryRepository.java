package com.example.food.repo;

import com.example.food.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    boolean existsByNameAndDeletedFalse(String name);

    // Klient uchun: Ham o'chmagan, ham aktiv bo'lishi shart
    List<Category> findAllByDeletedFalseAndActiveTrueOrderBySortOrderAsc();

    // Admin uchun: O'chmagan bo'lsa bo'ldi (aktiv-noaktivligidan qat'iy nazar)
    List<Category> findAllByDeletedFalseOrderBySortOrderAsc();

    Optional<Category> findByIdAndDeletedFalse(UUID id);


    Page<Category> findAllByDeletedFalse(Pageable pageable);

    List<Category> findAllByIdInAndDeletedFalse(List<UUID> id);
}