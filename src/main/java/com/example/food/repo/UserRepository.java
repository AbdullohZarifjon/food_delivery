package com.example.food.repo;

import com.example.food.entity.User;
import com.example.food.entity.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    // Optional ishlatish - eng xavfsiz va zamonaviy yo'l
    Optional<User> findByPhoneNumber(String phoneNumber);

    // Telefon raqami va ismni (katta-kichikligiga qaramasdan) qidirish
    Optional<User> findByPhoneNumberAndNameIgnoreCase(String phoneNumber, String name);

    @EntityGraph(attributePaths = {"roles"}) // Rollarni sessiya ochiqligida JOIN qilib oladi
    Optional<User> findByUsername(String username);

    boolean existsUserByUsername(String username);

    @Query("SELECT u FROM User u JOIN u.roles r " +
            "WHERE r.role = :role AND u.deleted = false")
    Page<User> findAllByRoles_Role(@Param("role") UserRole role, Pageable pageable);

    User findUserById(UUID id);

    boolean existsByPhoneNumber(String phoneNumber);

    @EntityGraph(attributePaths = {"roles"})
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdWithRoles(@Param("id") UUID id);

    Optional<User> findByIdAndDeletedFalse(UUID id);
}