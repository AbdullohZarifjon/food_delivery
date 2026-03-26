package com.example.food.controller;

import com.example.food.dto.BannerDto;
import com.example.food.service.BannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/customer/banners")
@Tag(name = "Banner Controller", description = "APIs for managing banners")
public class BannerCustomerController {

    private final BannerService bannerService;

    public BannerCustomerController(BannerService bannerService) {
        this.bannerService = bannerService;
    }

    @GetMapping("/active")
    @Operation(summary = "Hamma aktiv bannerlarni olish (Kesh bilan)")
    public ResponseEntity<List<BannerDto.BannerResponse>> getActiveBanners() {
        return ResponseEntity.ok(bannerService.getActiveBanners());
    }

}
