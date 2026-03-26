package com.example.food.controller;

import com.example.food.dto.ProductDto;
import com.example.food.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/v1/admin/products")
@Tag(name = "Product Admin API")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class ProductAdminController {

    private final ProductService productService;

    public ProductAdminController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Admin: Product qo'shish")
    public ResponseEntity<ProductDto.ProductResponse> create(
            @RequestPart("data") @Valid ProductDto.ProductRequest productRequest,
            @RequestPart(value = "file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(productRequest, file));
    }


    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Admin: Productni tahrir qilish")
    public ResponseEntity<ProductDto.ProductResponse> update(
            @PathVariable UUID id,
            @RequestPart("data") @Valid ProductDto.ProductRequest productRequest,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        return ResponseEntity.ok(productService.update(id, productRequest, file));
    }


    @GetMapping("/{id}")
    @Operation(summary = "Mijoz va Admin uchun: Mahsulotni ID bo'yicha olish")
    public ResponseEntity<ProductDto.ProductResponse> getByIdForAdmin(
            @PathVariable @NotNull(message = "ID bo'sh bo'lishi mumkin emas") UUID id) {
        return ResponseEntity.ok(productService.getByIdForAdmin(id));
    }


    @GetMapping
    @Operation(summary = "Admin: Barcha mahsulotlarni sahifalangan holda olish")
    public ResponseEntity<Page<ProductDto.ProductResponse>> getAll(
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable) {
        return ResponseEntity.ok(productService.getAllForAdmin(pageable));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable @NotNull(message = "ID bo'sh bo'lishi mumkin emas") UUID id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }


    @PatchMapping("/{id}/price")
    @Operation(summary = "Admin: Narxni tezkor yangilash")
    public ResponseEntity<Void> updatePrice(
            @PathVariable @NotNull(message = "ID bo'sh bo'lishi mumkin emas") UUID id,
            @RequestParam @Positive(message = "Narx musbat bo'lishi shart") BigDecimal price) {
        productService.updatePrice(id, price);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Admin: Mahsulot statusini o'zgartirish (Active/Inactive)")
    public ResponseEntity<Void> toggleStatus(
            @PathVariable @NotNull(message = "ID bo'sh bo'lishi mumkin emas") UUID id,
            @RequestParam boolean active) {
        productService.changeStatus(id, active);
        return ResponseEntity.ok().build();
    }

}