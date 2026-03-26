package com.example.food.service.impl;

import com.example.food.dto.CategoryDto;
import com.example.food.entity.Category;
import com.example.food.exception.RecordAlreadyException;
import com.example.food.exception.RecordNotFoundException;
import com.example.food.mapper.CategoryMapper;
import com.example.food.repo.CategoryRepository;
import com.example.food.service.CategoryService;
import com.example.food.service.FileService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final FileService fileService;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CATEGORY_CACHE_KEY = "active_categories_list";

    public CategoryServiceImpl(CategoryRepository categoryRepository, CategoryMapper categoryMapper, FileService fileService, RedisTemplate<String, Object> redisTemplate) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
        this.fileService = fileService;
        this.redisTemplate = redisTemplate;
    }

    @Override
    @Transactional
    public CategoryDto.CategoryResponse create(CategoryDto.CategoryRequest categoryRequest, MultipartFile file) {
        // 1. Unikallikni tekshirish
        existsByName(categoryRequest.name());

        // 2. Mapping
        Category category = categoryMapper.toEntity(categoryRequest);

        // 3. File upload (Controller-dagi tekshiruvdan o'tgani uchun ishonch bilan yuklaymiz)
        String fileName = "categories" + "/" + UUID.randomUUID() + ".jpg";
        category.setImageUrl(fileService.uploadFile(file, fileName));

        // 4. Save & Cache clear
        category = categoryRepository.save(category);
        redisTemplate.delete(CATEGORY_CACHE_KEY);

        return categoryMapper.toResponse(category);
    }


    @Override
    @Transactional
    public CategoryDto.CategoryResponse update(UUID id, CategoryDto.UpdateCategoryRequest request, MultipartFile file) {
        // 1. Faqat o'chirilmagan kategoriyani qidiramiz
        Category category = findById(id);

        // 2. Nom o'zgargan bo'lsa, unikallikni tekshiramiz
        if (!category.getName().equalsIgnoreCase(request.name())) {
            existsByName(request.name());
            category.setName(request.name());
        }

        category.setActive(request.active());
        category.setSortOrder(request.sortOrder());

        // 3. File handling - tartibga e'tibor bering
        if (file != null && !file.isEmpty()) {
            String oldImageUrl = category.getImageUrl();
            // Yangisini yuklaymiz
            String fileName = "categories" + "/" + UUID.randomUUID() + ".jpg";
            String newImageUrl = fileService.uploadFile(file, fileName);
            category.setImageUrl(newImageUrl);

            // Eskisini faqat yangisi muvaffaqiyatli bo'lsagina o'chiramiz
            if (oldImageUrl != null) {
                fileService.deleteFile(oldImageUrl);
            }
        }

        categoryRepository.save(category);
        redisTemplate.delete(CATEGORY_CACHE_KEY);

        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto.CategoryResponse> getActiveCategories() {
        try {
            Object cachedData = redisTemplate.opsForValue().get(CATEGORY_CACHE_KEY);

            if (cachedData != null) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());

                // Senior mantiq: Kelgan ob'ektni List-ga xavfsiz konvertatsiya qilish
                List<CategoryDto.CategoryResponse> categories = mapper.convertValue(
                        cachedData,
                        new TypeReference<List<CategoryDto.CategoryResponse>>() {}
                );

                System.out.println("------------------------------------------");
                System.out.println("🚀 REDIS: Kategoriyalar keshdan olindi!");
                System.out.println("------------------------------------------");
                return categories;
            }
        } catch (Exception e) {
            System.err.println("❌ REDIS ERROR: O'qishda xato: " + e.getMessage());
            redisTemplate.delete(CATEGORY_CACHE_KEY);
        }

        // 2. DB dan olish
        System.out.println("------------------------------------------");
        System.out.println("💾 DATABASE: Ma'lumotlar bazasidan yuklanmoqda...");
        System.out.println("------------------------------------------");

        List<CategoryDto.CategoryResponse> categoriesFromDb = categoryRepository.findAllByDeletedFalseAndActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(categoryMapper::toResponse)
                .toList();

        // 3. Keshga yozish
        if (!categoriesFromDb.isEmpty()) {
            try {
                redisTemplate.opsForValue().set(CATEGORY_CACHE_KEY, categoriesFromDb, Duration.ofDays(1));
                System.out.println("✅ REDIS: Keshga yozildi.");
            } catch (Exception e) {
                System.err.println("❌ REDIS ERROR: Yozishda xato: " + e.getMessage());
            }
        }

        return categoriesFromDb;
    }


    @Override
    @Transactional
    public void delete(UUID id) {
        // 1. Faqat tirik kategoriyani topamiz
        Category category = findById(id);

        // 2. Bog'lanishlarni uzish (ManyToMany bo'lgani uchun)
        // Mahsulotlar o'chmaydi, lekin bu kategoriya ulardan ajraladi
        category.getProducts().clear();

        // 3. Soft Delete mantiqi
        category.setDeleted(true);
        category.setActive(false);
        // BaseEntity-dagi @PreUpdate isDeleted bo'lganda deletedAt ni o'zi qo'yadi

        // 4. Rasmni O'CHIRMAYMIZ!
        // Chunki bu Soft Delete. Ma'lumot bazada tursa, rasm ham turishi kerak.

        categoryRepository.save(category);

        // 5. Keshni tozalash
        redisTemplate.delete(CATEGORY_CACHE_KEY);
    }


    @Override
    public CategoryDto.CategoryResponse getById(UUID id) {
        return categoryMapper.toResponse(findById(id));
    }


    @Override
    @Transactional(readOnly = true)
    public Page<CategoryDto.CategoryResponse> getAllForAdmin(Pageable pageable) {
        // Page obyektini map qilish juda oson:
        return categoryRepository.findAllByDeletedFalse(pageable)
                .map(categoryMapper::toResponse);
    }


    @Override
    public List<Category> getCategoriesByIdInAndIsDeletedFalse(List<UUID> ids) {
        return categoryRepository.findAllByIdInAndDeletedFalse(ids);
    }


    private void existsByName(String name) {
        if (categoryRepository.existsByNameAndDeletedFalse(name)) {
            throw new RecordAlreadyException("Bunday nomli kategoriya mavjud!");
        }
    }

    private Category findById(UUID id) {
        return categoryRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RecordNotFoundException("Kategoriya topilmadi"));
    }
}
