package com.example.food.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.locationtech.jts.geom.Point;

@Getter
@Setter
@Entity
@SuperBuilder
@Table(name = "branches")
@NoArgsConstructor
@AllArgsConstructor
public class Branch extends BaseEntity {
    private String name;
    private String address;

    @Column(columnDefinition = "geometry(Point,4326)")
    private Point location;

    @Builder.Default
    private boolean main = false; // Asosiy filialmi?

    @Builder.Default
    private boolean active = true;
}
