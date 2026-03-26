package com.example.food.entity;

import com.example.food.entity.enums.MeasurementUnit;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "products")
public class Product extends BaseEntity implements Serializable {

    @Column(nullable = false)
    private String name;

    private String description;
    private String imageUrl;

    @Column(nullable = false)
    private BigDecimal price;

//    private Integer stockQuantity;

    @Enumerated(EnumType.STRING)
    private MeasurementUnit unit;

    @ManyToMany
    @JoinTable(
            name = "product_categories",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<Category> categories;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id")
    private Promotion promotion; // Faqat bog'lanish (Optional)

    private boolean active = true;

    public BigDecimal calculateCurrentPrice() {
        if (promotion == null || !promotion.getActive() || promotion.isDeleted()) {
            return this.price;
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(promotion.getStartDate()) || now.isAfter(promotion.getEndDate())) {
            return this.price;
        }

        BigDecimal discount = BigDecimal.ZERO;
        if (promotion.getDiscountPercentage() != null) {
            discount = this.price.multiply(BigDecimal.valueOf(promotion.getDiscountPercentage()))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else if (promotion.getFixedDiscountAmount() != null) {
            discount = promotion.getFixedDiscountAmount();
        }

        BigDecimal finalPrice = this.price.subtract(discount);
        return finalPrice.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : finalPrice;
    }
}
