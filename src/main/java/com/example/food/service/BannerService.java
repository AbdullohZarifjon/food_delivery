package com.example.food.service;

import com.example.food.dto.BannerDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface BannerService {

    BannerDto.BannerResponse createBanners(BannerDto.BannerRequest bannerDto, MultipartFile file);

    BannerDto.BannerResponse getBannerById(UUID id);

    List<BannerDto.BannerResponse> getAllBannersForAdmin();

    List<BannerDto.BannerResponse> getActiveBanners();

    BannerDto.BannerResponse updateBanner(UUID id, BannerDto.UpdateBannerRequest bannerDto, MultipartFile file);

    void deleteBanner(UUID id);
}
