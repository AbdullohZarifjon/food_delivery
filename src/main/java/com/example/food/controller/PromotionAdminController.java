package com.example.food.controller;

import com.example.food.dto.ProductDto;
import com.example.food.dto.PromotionDto;
import com.example.food.service.PromotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/v1/admin/promotions")
@Tag(name = "Promotion Admin API")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class PromotionAdminController {

    private final PromotionService promotionService;

    public PromotionAdminController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @PostMapping
    @Operation(summary = "Admin: Aksiya promotion qo'shish")
    public ResponseEntity<PromotionDto.PromotionResponse> create(@Valid @RequestBody PromotionDto.PromotionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(promotionService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PromotionDto.PromotionResponse> update(
            @PathVariable @NotNull(message = "Aksiya ID raqami yuborilishi shart") UUID id,
            @Valid @RequestBody PromotionDto.PromotionRequest request) {
        return ResponseEntity.ok(promotionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable @NotNull(message = "Aksiya ID raqami yuborilishi shart") UUID id) {
        promotionService.delete(id);
        return ResponseEntity.noContent().build();
    }


    @PatchMapping("/{id}/assign-products")
    @Operation(summary = "Admin: Aksiyani mahsulotlarga biriktirish")
    public ResponseEntity<Void> assignToProducts(
            @PathVariable @NotNull(message = "Aksiya ID raqami yuborilishi shart") UUID id,
            @RequestBody
            @NotEmpty(message = "Mahsulotlar ro'yxati bo'sh bo'lishi mumkin emas")
            List<@NotNull(message = "Mahsulot ID raqami null bo'lishi mumkin emas") UUID> productIds) {
        promotionService.assignPromotionToProducts(id, productIds);
        return ResponseEntity.ok().build();
    }


    // 1. ID orqali olish
    @GetMapping("/{id}")
    @Operation(summary = "Admin: Aksiyani ID orqali ko'rish")
    public ResponseEntity<PromotionDto.PromotionResponse> getById(
            @PathVariable @NotNull(message = "Aksiya ID raqami yuborilishi shart") UUID id) {
        return ResponseEntity.ok(promotionService.getById(id));
    }


    @GetMapping
    @Operation(summary = "Admin: Barcha aksiyalar ro'yxati (Pagination)")
    public ResponseEntity<Page<PromotionDto.PromotionResponse>> getAll(
            @ParameterObject
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(promotionService.getAll(pageable));
    }


    // 3. Statusni o'zgartirish (Toggle)
    @PatchMapping("/{id}/status")
    @Operation(summary = "Admin: Aksiya holatini o'zgartirish (active true/false)")
    public ResponseEntity<Void> changeStatus(
            @PathVariable @NotNull(message = "Aksiya ID raqami yuborilishi shart") UUID id,
            @RequestParam boolean active) {
        promotionService.changeStatus(id, active);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/{id}/products")
    @Operation(summary = "Admin: Muayyan aksiyaga biriktirilgan mahsulotlar ro'yxati")
    public ResponseEntity<Page<ProductDto.ProductResponse>> getAssignedProducts(
            @PathVariable @NotNull(message = "Aksiya ID raqami yuborilishi shart") UUID id,
            @ParameterObject
            @PageableDefault(size = 10)
            Pageable pageable) {
        return ResponseEntity.ok(promotionService.getAssignedProducts(id, pageable));
    }

}
