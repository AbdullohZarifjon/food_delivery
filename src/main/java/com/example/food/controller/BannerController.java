package com.example.food.controller;

import com.example.food.dto.BannerDto;
import com.example.food.exception.BadRequestException;
import com.example.food.service.BannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/banners")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Tag(name = "Banner Controller For Admin", description = "APIs for managing banners")
public class BannerController {

    private final BannerService bannerService;

    public BannerController(BannerService bannerService) {
        this.bannerService = bannerService;
    }


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "ADMIN: Yangi banner qo'shish")
    public ResponseEntity<BannerDto.BannerResponse> createBanners(
            @RequestPart("data") @Valid BannerDto.BannerRequest bannerDto,
            @RequestPart("file") MultipartFile file) {

        // 1. Fayl bo'sh emasligini tekshirish (fail-fast)
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Banner uchun rasm yuklanishi shart!");
        }

        // 2. Fayl turini tekshirish (Optional, lekin tavsiya etiladi)
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("Faqat rasm formatidagi fayllar qabul qilinadi!");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(bannerService.createBanners(bannerDto, file));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Admin: Banner ma'lumotlarini ID orqali ko'rish")
    public ResponseEntity<BannerDto.BannerResponse> getBannerById(@PathVariable UUID id) {
        return ResponseEntity.ok(bannerService.getBannerById(id));
    }


    @GetMapping("/all")
    @Operation(summary = "Hamma bannerlarni olish (Kesh emas)")
    public ResponseEntity<List<BannerDto.BannerResponse>> getAllBanners() {
        return ResponseEntity.ok(bannerService.getAllBannersForAdmin());
    }


    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "ADMIN: Bannerni tahrirlash (Active holati va rasm)")
    public ResponseEntity<BannerDto.BannerResponse> updateBanner(
            @PathVariable UUID id,
            @RequestPart("data") @Valid BannerDto.UpdateBannerRequest updateBannerRequest, // Yangi DTO
            @RequestPart(value = "file", required = false) MultipartFile file) {

        return ResponseEntity.ok(bannerService.updateBanner(id, updateBannerRequest, file));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "ADMIN: Bannerni butunlay o'chirish")
        public ResponseEntity<Void> deleteBanner(@PathVariable UUID id) {
        bannerService.deleteBanner(id);
        return ResponseEntity.noContent().build(); // 204 No Content qaytarish - eng to'g'ri REST yo'li
    }

}
