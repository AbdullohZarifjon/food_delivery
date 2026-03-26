package com.example.food.entity;

import com.example.food.entity.enums.OrderStatus;
import com.example.food.entity.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders", indexes = {
        @Index(name = "idx_order_status", columnList = "status")   // Status bo'yicha filterlash uchun
})
@ToString(callSuper = true, exclude = {"customer", "courier", "items"})
public class Order extends BaseEntity {

    // Kurer va Mijoz muloqoti uchun unikal qisqa kod (#54218)
    @Column(unique = true, nullable = false, length = 10)
    private String orderCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @Column(nullable = false)
    private String username;

    // Buyurtmani yetkazayotgan kurer (Hali biriktirilmagan bo'lsa null bo'ladi)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courier_id")
    private Courier courier;

    // --- SNAPSHOTS (Tarix o'zgarmasligi uchun) ---

    @Column(nullable = false)
    private String customerName; // Mijoz ismini o'zgartirsa ham buyurtmadagi ismi qoladi

    @Column(nullable = false)
    private String contactPhone;        // Buyurtma berilgan vaqtdagi telefon (Mijoz yuborgan)

    private String courierName;  // Yetkazgan kurerning ismi (Snapshot)
    private String courierPhoneAtOrder; // Kurerning o'sha vaqtdagi telefoni

    @Column(nullable = false)
    private String deliveryAddressName; // Manzilning matnli ko'rinishi (Uy, Ish, Chilonzor...)

    // --- GEOSPATIAL DATA ---

    @Column(columnDefinition = "geometry(Point,4326)", nullable = false)
    private Point deliveryAddress;     // PostGIS: Xaritadagi aniq nuqta (Lon, Lat)

    @Column(nullable = false, precision = 19, scale = 2)
    @DecimalMin(value = "0.0", message = "Dostavka narxi manfiy bo'lmaydi")
    private BigDecimal deliveryFee;    // Yetkazib berish uchun to'langan haq

    @PositiveOrZero(message = "Masofa manfiy bo'lishi mumkin emas")
    private Double distance;           // Restoran va mijoz orasidagi masofa (km)

    // --- TOTALS & ITEMS ---

    // CascadeType.ALL: Order o'chirilsa yoki saqlansa, itemlar ham birga ishlanadi
    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY // Odatda yozish shart emas, lekin tushunarli bo'lishi uchun
    )    private List<OrderItem> items;

    @Column(nullable = false, precision = 19, scale = 2)
    @DecimalMin(value = "0.0")
    private BigDecimal totalPrice;     // Jami summa: (Ovqatlar + Dostavka)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING; // Buyurtma holati

    private String cancelledByName; // Bekor qilgan foydalanuvchining ID-si

    @Column(name = "cancelled_by_role")
    private UserRole cancelledByRole; // "ADMIN" yoki "CUSTOMER"

    @Column(name = "cancellation_reason")
    @Size(max = 500)
    private String cancellationReason;

}