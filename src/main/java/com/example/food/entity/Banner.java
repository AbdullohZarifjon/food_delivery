package com.example.food.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "banners")
public class Banner extends BaseEntity implements Serializable {

    private String title;      // Reklama sarlavhasi (ixtiyoriy)
    private String imageUrl;   // Rasmni URL manzili

    private String redirectUrl; // Rasm bosilganda qayerga o'tishi (masalan, maxsus aksiyadagi mahsulotga)

    private Integer sortOrder;  // Karuselda nechanchi bo'lib chiqishi

    private LocalDateTime expiresAt; // Reklama muddati (tugagach avtomatik chiqmaydi)

    @Builder.Default
    private boolean active = true;
}
