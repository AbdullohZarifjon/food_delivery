package com.example.food.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "reviews")
@ToString(callSuper = true, exclude = {"order", "customer"})
public class Review extends BaseEntity implements Serializable {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    // Ovqat uchun rating (1-5 gacha)
    private Integer foodRating;
    private String foodComment;

    // Kurer uchun rating (1-5 gacha)
    private Integer deliveryRating;
    private String deliveryComment;

    @Builder.Default
    private boolean isVisible = true; // Nojo'ya so'zlar bo'lsa admin yashirib qo'yishi uchun
}
