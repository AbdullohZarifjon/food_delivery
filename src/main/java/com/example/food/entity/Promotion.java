package com.example.food.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "promotions", indexes = {
        @Index(name = "idx_promotion_dates", columnList = "startDate, endDate"),
        @Index(name = "idx_promotion_active", columnList = "active")
})
public class Promotion extends BaseEntity implements Serializable {

    @NotBlank(message = "Aksiya nomi bo'sh bo'lmasligi shart")
    private String title;

    @Min(value = 1, message = "Chegirma foizi 1 dan kam bo'lmasligi shart")
    @Max(value = 100, message = "Chegirma foizi 100 dan oshmasligi shart")
    private Integer discountPercentage;

    @Positive(message = "Chegirma summasi musbat bo'lishi shart")
    private BigDecimal fixedDiscountAmount;

    @NotNull(message = "Boshlanish vaqti shart")
    private LocalDateTime startDate;

    @NotNull(message = "Tugash vaqti shart")
    private LocalDateTime endDate;

    @Builder.Default
    private Boolean active = true;

    @OneToMany(mappedBy = "promotion", fetch = FetchType.LAZY)
    private List<Product> products;

    // Senior tavsiya: Ikkala chegirma turi ham bo'sh yoki ikkalasi ham to'la bo'lmasligi uchun
    @AssertTrue(message = "Yo foiz, yo aniq summa chegirmasi kiritilishi shart")
    private boolean isValidDiscount() {
        return (discountPercentage != null && fixedDiscountAmount == null) ||
                (discountPercentage == null && fixedDiscountAmount != null);
    }
}