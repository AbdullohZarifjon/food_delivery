package com.example.food.entity;

import com.example.food.entity.enums.MeasurementUnit;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter @Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "order_items")
public class OrderItem extends BaseEntity implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private UUID productId;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private String productImageUrl;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal originalPriceAtOrder;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal priceAtOrder;

    @Column(nullable = false)
    @Min(1)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    private MeasurementUnit unitSnapshot;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal rowTotal;
}
