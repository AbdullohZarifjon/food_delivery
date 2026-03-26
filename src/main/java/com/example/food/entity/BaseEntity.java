package com.example.food.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@MappedSuperclass
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder // @Builder o'rniga
public abstract class BaseEntity {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id; // ID ni ham shu yerga ko'chirish mumkin

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    @Builder.Default
    private boolean deleted = false;

    // Avtomatlashtirish qismi:
    @PreUpdate
    @PrePersist
    public void preUpdate() {
        if (this.deleted && this.deletedAt == null) {
            this.deletedAt = LocalDateTime.now();
        }
    }
//    @PreUpdate: Har safar save() yoki update bo'layotganda ishlaydi. Agar isDeleted true bo'lsa-yu,
//    deletedAt hali null bo'lsa, u hozirgi vaqtni yozib qo'yadi.
//
//    @PrePersist: Yangi object yaratilayotganda ham tekshiradi (kamdan-kam hollarda kerak bo'ladi, lekin ehtiyot shart).
}
