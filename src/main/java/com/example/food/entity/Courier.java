package com.example.food.entity;

import com.example.food.entity.enums.CourierStatus;
import com.example.food.entity.enums.CourierType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Getter
@Setter
@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true, exclude = "user")
@Table(name = "couriers",
        indexes = @Index(name = "idx_courier_status", columnList = "status"))
public class Courier extends BaseEntity implements Serializable {

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // Mana bu sehrli tayoqcha! User-ning ID-sini olib, Courier-ning ID-siga yopishtiradi.
    @JoinColumn(name = "id") // Courier jadvalidagi 'id' ustuni ham PK, ham FK bo'ladi
    private User user;

    @Enumerated(EnumType.STRING)
    private CourierType courierType;

    private String vehicleNumber;
    private String carModel;

    @Enumerated(EnumType.STRING)
    private CourierStatus status;

    public Courier(User user) {
        this.user = user;
    }
}
