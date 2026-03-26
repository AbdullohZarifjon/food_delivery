package com.example.food.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "categories")
@ToString(callSuper = true, exclude = "products")
public class Category extends BaseEntity implements Serializable {

    @Column(nullable = false)
    private String name;

    private String imageUrl;

    @ManyToMany(mappedBy = "categories") // Product'dagi nom bilan bir xil bo'lishi kerak
    private List<Product> products;

    private Integer sortOrder = 0;

    private boolean active = true; // Admin uchun kerakli maydon
}
