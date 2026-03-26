package com.example.food.repo;

import com.example.food.entity.Branch;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BranchRepository extends JpaRepository<Branch, Long> {

    // Birinchi asosiy va aktiv filialni topish
    Optional<Branch> findFirstByMainTrueAndActiveTrue();

    // Agar asosiy belgilanmagan bo'lsa, shunchaki birinchi aktivini olish uchun (fallback)
    Optional<Branch> findFirstByActiveTrue();

    // PostGIS orqali masofani hisoblash (SQL darajasida juda tez ishlaydi)
    @Query(value = "SELECT ST_DistanceSphere(:p1, :p2)", nativeQuery = true)
    Double calculateDistance(@Param("p1") Point p1, @Param("p2") Point p2);
}