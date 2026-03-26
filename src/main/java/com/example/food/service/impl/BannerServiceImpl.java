package com.example.food.service.impl;

import com.example.food.dto.BannerDto;
import com.example.food.entity.Banner;
import com.example.food.exception.RecordNotFoundException;
import com.example.food.mapper.BannerMapper;
import com.example.food.repo.BannerRepository;
import com.example.food.service.BannerService;
import com.example.food.service.FileService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
public class BannerServiceImpl implements BannerService {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String BANNER_CACHE_KEY = "active_banners";
    private final FileService fileService;
    private final BannerRepository bannerRepository;
    private final BannerMapper bannerMapper;

    public BannerServiceImpl(RedisTemplate<String, Object> redisTemplate, FileService fileService, BannerRepository bannerRepository, BannerMapper bannerMapper) {
        this.redisTemplate = redisTemplate;
        this.fileService = fileService;
        this.bannerRepository = bannerRepository;
        this.bannerMapper = bannerMapper;
    }

    @Override
    @Transactional
    public BannerDto.BannerResponse createBanners(BannerDto.BannerRequest bannerDto, MultipartFile file) {
        // 1. DTO -> Entity
        Banner banner = bannerMapper.toEntity(bannerDto);

        // 2. Rasmni MinIO/S3 ga yuklash va URL ni olish
        // "banners" - bu MinIO dagi bucket yoki papka nomi bo'ladi
        String fileName = "banners" + "/" + UUID.randomUUID() + ".jpg";
        String imageUrl = fileService.uploadFile(file, fileName);
        banner.setImageUrl(imageUrl);

        // 3. Bazaga saqlash
        bannerRepository.save(banner);

        // 4. Cache Eviction (Keshni tozalash)
        // Yangi banner qo'shilganda eski keshni o'chirib yuboramiz
        redisTemplate.delete(BANNER_CACHE_KEY);

        return bannerMapper.toResponse(banner);
    }


    @Override
    @Transactional(readOnly = true) // DB yukini kamaytirish uchun
    public BannerDto.BannerResponse getBannerById(UUID id) {
        Banner banner = findById(id);

        return bannerMapper.toResponse(banner);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BannerDto.BannerResponse> getAllBannersForAdmin() {

        // 2. DBdan olamiz
        return bannerRepository.findAll()
                .stream()
                .map(bannerMapper::toResponse)
                .toList();
    }


    @Override
    @Transactional(readOnly = true)
    public List<BannerDto.BannerResponse> getActiveBanners() {
        try {
            // 1. Keshdan o'qiymiz
            Object cachedData = redisTemplate.opsForValue().get(BANNER_CACHE_KEY);
            if (cachedData instanceof List) {
                return (List<BannerDto.BannerResponse>) cachedData;
            }
        } catch (Exception e) {
//            log.error("Redis o'qishda xato: ", e);
            System.out.println("Redis o'qishda xato: ");
        }

        // 2. DBdan olamiz
        List<BannerDto.BannerResponse> banners = bannerRepository.findAllByActiveTrue()
                .stream()
                .map(bannerMapper::toResponse)
                .toList();

        // 3. Redisga ABADIY saqlaymiz (Vaqt ko'rsatmaymiz)
        if (!banners.isEmpty()) {
            try {
                redisTemplate.opsForValue().set(BANNER_CACHE_KEY, banners);
            } catch (Exception e) {
//                log.error("Redisga yozishda xato: ", e);
                System.out.println("Redisga yozishda xato: ");
            }
        }

        return banners;
    }

    @Override
    @Transactional
    public BannerDto.BannerResponse updateBanner(UUID id, BannerDto.UpdateBannerRequest updateBannerRequest, MultipartFile file) {
        Banner banner = findById(id);

        // 1. Eski URL-ni saqlab qo'yamiz
        String oldImageUrl = banner.getImageUrl();

        banner.setSortOrder(updateBannerRequest.sortOrder());
        banner.setExpiresAt(updateBannerRequest.expiresAt());
        banner.setActive(updateBannerRequest.isActive());

        // 2. Agar yangi fayl kelsa
        if (file != null && !file.isEmpty()) {
            // Yangi rasmni yuklaymiz
            String fileName = "banners" + "/" + UUID.randomUUID() + ".jpg";
            String newImageUrl = fileService.uploadFile(file, fileName);
            banner.setImageUrl(newImageUrl);

            // 3. Yangi rasm muvaffaqiyatli yuklangandan so'ng, eskisini o'chiramiz
            try {
                fileService.deleteFile(oldImageUrl);
            } catch (Exception e) {
                // Eski rasm o'chmasa ham update to'xtab qolmasligi kerak (soft fail)
                System.err.println("Eski rasmni o'chirishda xato: " + e.getMessage());
            }
        }

        bannerRepository.save(banner);
        redisTemplate.delete(BANNER_CACHE_KEY);

        return bannerMapper.toResponse(banner);
    }

    @Override
    @Transactional
    public void deleteBanner(UUID id) {
        // 1. O'chirishdan oldin bannerni topamiz (rasm URL-ini olish uchun)
        Banner banner = findById(id);

        String imageUrl = banner.getImageUrl();

        // 2. Bazadan o'chiramiz
        bannerRepository.delete(banner);

        // 3. MinIO-dan rasmni o'chiramiz
        try {
            fileService.deleteFile(imageUrl);
        } catch (Exception e) {
            // Rasm o'chmasa ham foydalanuvchiga xato bermaymiz, faqat log qilamiz
            System.err.println("Banner rasmini MinIO'dan o'chirishda xato: " + e.getMessage());
        }

        // 4. Keshni tozalaymiz
        redisTemplate.delete(BANNER_CACHE_KEY);
    }

    private Banner findById(UUID id) {
        return bannerRepository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException("Banner topilmadi! ID: " + id));
    }
}
