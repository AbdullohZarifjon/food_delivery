package com.example.food.controller;

import com.example.food.dto.ProductDto;
import com.example.food.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Product Customer API", description = "Mijozlar uchun mahsulotlar ro'yxati va qidiruv")
@Validated // Method darajasidagi validatsiyalar ishlashi uchun shart
public class ProductCustomerController {

    private final ProductService productService;

    public ProductCustomerController(ProductService productService) {
        this.productService = productService;
    }

    
    @GetMapping("/{id}")
    @Operation(summary = "Barcha uchun: Mahsulotni ID bo'yicha olish")
    public ResponseEntity<ProductDto.ProductShortResponse> getById(
            @PathVariable @NotNull(message = "Mahsulot ID raqami yuborilishi shart") UUID id) {
        return ResponseEntity.ok(productService.getById(id));
    }


//    @GetMapping("/category/{categoryId}")
//    @Operation(summary = "Mijoz uchun: Kategoriya bo'yicha mahsulotlarni olish")
//    public ResponseEntity<Page<ProductDto.ProductShortResponse>> getByCategoryId(
//            @PathVariable @NotNull(message = "Kategoriya ID raqami yuborilishi shart") UUID categoryId,
//            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
//        return ResponseEntity.ok(productService.getProductsByCategoryId(categoryId, pageable));
//    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Mijoz uchun: Kategoriya bo'yicha 5 ta mahsulotni olish")
    public ResponseEntity<List<ProductDto.ProductShortResponse>> getByCategoryId(
            @PathVariable UUID categoryId,
            @RequestParam(defaultValue = "0") int page) {

        return ResponseEntity.ok(productService.getProductsByCategoryId(categoryId, page));
    }


    @GetMapping("/search")
    @Operation(summary = "Mijoz uchun: Nomi bo'yicha 5 tadan qidirish")
    public ResponseEntity<List<ProductDto.ProductShortResponse>> search(
            @RequestParam @NotBlank(message = "Qidiruv so'zi bo'sh bo'lishi mumkin emas") @Size(min = 2) String name,
            @RequestParam(defaultValue = "0") int page) {

        return ResponseEntity.ok(productService.searchByName(name, page));
    }
}
