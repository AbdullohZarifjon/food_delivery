package com.example.food.repo;

import com.example.food.entity.Order;
import com.example.food.entity.enums.OrderStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    boolean existsByOrderCode(String orderCode);

    @EntityGraph(attributePaths = {"items"})
    List<Order> findAllByStatusInOrderByCreatedAtDesc(List<OrderStatus> activeStatuses);

    @EntityGraph(attributePaths = {"items"})
    List<Order> findAllByCourierUserIdAndStatusInAndDeletedFalseOrderByCreatedAtDesc(
            UUID courierUserId,
            List<OrderStatus> statuses
    );

    @EntityGraph(attributePaths = {"items"})
    List<Order> findAllByCustomerIdAndStatusInAndDeletedFalse(UUID id, List<OrderStatus> activeStatuses);

    @EntityGraph(attributePaths = {"items"})
    Optional<Order> findByIdAndDeletedFalse(UUID id);

    @EntityGraph(attributePaths = {"items"})
    Optional<Order> findByOrderCodeAndDeletedFalse(String orderCode);

    // OrderRepository.java
    @EntityGraph(attributePaths = {"items"})
    List<Order> findAllByCourierUserIdAndStatusAndCreatedAtBetweenAndDeletedFalseOrderByCreatedAtDesc(
            UUID courierUserId,
            OrderStatus status,
            LocalDateTime start,
            LocalDateTime end
    );

    List<Order> findAllByCourierIdAndStatus(UUID courierId, OrderStatus orderStatus);

    /**
     * Kuryer va Mijoz o'rtasida 'ON_THE_WAY' statusli bog'liqlik borligini tekshirish.
     * Bu metod SQL-da 'SELECT 1 FROM orders ... LIMIT 1' ko'rinishida ishlaydi.
     */
    @Query("""
    SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END 
    FROM Order o 
    WHERE ((o.courier.id = :senderId AND o.customer.id = :recipientId) 
       OR (o.courier.id = :recipientId AND o.customer.id = :senderId))
    AND o.status = 'ON_THE_WAY'
    """)
    boolean existsActiveDelivery(UUID senderId, UUID recipientId);

}