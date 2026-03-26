package com.example.food.repo;

import com.example.food.entity.Courier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface CourierRepository extends JpaRepository<Courier, UUID> {

    @EntityGraph(attributePaths = {"user"}) // user.roles allaqachon EAGER, uni yozish shart emas
    @Query(value = "SELECT c FROM Courier c WHERE c.deleted = false AND c.user.deleted = false",
            countQuery = "SELECT count(c) FROM Courier c WHERE c.deleted = false AND c.user.deleted = false")
    Page<Courier> findAllActiveWithUser(Pageable pageable);


    @EntityGraph(attributePaths = {"user"})
    Optional<Courier> findByUserId(UUID userId);

}