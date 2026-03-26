package com.example.food.service.impl;

import com.example.food.dto.ProductDto;
import com.example.food.entity.Category;
import com.example.food.entity.Product;
import com.example.food.entity.Promotion;
import com.example.food.exception.BadRequestException;
import com.example.food.exception.RecordAlreadyException;
import com.example.food.exception.RecordNotFoundException;
import com.example.food.mapper.ProductMapper;
import com.example.food.repo.ProductRepository;
import com.example.food.service.CategoryService;
import com.example.food.service.FileService;
import com.example.food.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final FileService fileService;
    private final CategoryService categoryService;

    public ProductServiceImpl(ProductRepository productRepository, ProductMapper productMapper, FileService fileService, CategoryService categoryService) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.fileService = fileService;
        this.categoryService = categoryService;
    }


    @Override
    @Transactional
    @CacheEvict(value = {"products", "products-category", "products-search"}, allEntries = true)
    public ProductDto.ProductResponse create(ProductDto.ProductRequest productRequest, MultipartFile file) {
        // 1. Validatsiyalar (Private metodlarga chiqarilgan)
        validateProductUniqueness(productRequest.name());
        List<Category> categories = validateAndGetCategories(productRequest.categoryIds());

        // 2. Mapping va Entity tayyorlash
        Product product = productMapper.toEntity(productRequest);
        product.setCategories(categories);

        // 3. Fayl bilan ishlash (Alohida metod)
        handleProductImage(product, file);

        return productMapper.toResponse(productRepository.save(product));
    }


    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "products", key = "#id"),
            @CacheEvict(value = "products-category", allEntries = true),
            @CacheEvict(value = "products-search", allEntries = true)
    })
    public ProductDto.ProductResponse update(UUID id, ProductDto.ProductRequest productRequest, MultipartFile file) {
        Product product = findProductByIdForAdmin(id);

        // 1. Unikallikni tekshirish
        if (!product.getName().equalsIgnoreCase(productRequest.name())) {
            validateProductUniqueness(productRequest.name());
        }

        // 2. Kategoriyalarni yangilash
        List<Category> categories = validateAndGetCategories(productRequest.categoryIds());
        productMapper.updateEntity(product, productRequest);
        product.setCategories(categories);

        // 3. Rasmni aqlli yangilash
        if (file != null && !file.isEmpty()) {
            String imageUrl = product.getImageUrl();
            // delete qilish shart emas! MinIO ustiga yozib yuboradi (Overwrite)

            // 2. MinIO-ga o'sha nom bilan yangi faylni uramiz (Eskisini ustiga yozadi)
            fileService.uploadFile(file, imageUrl);

            // Product jadvalida imageUrl o'zgarmaydi, shuning uchun product.setImageUrl() shart emas!
            log.info("Rasm o'sha URL bilan MinIO'da yangilandi: {}", imageUrl);
        } else {
            // Agar oldin rasm bo'lmagan bo'lsa, yangi nom bilan yaratamiz
            handleProductImage(product, file);
        }

        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "products", key = "#id"), // Shaxsiy keshni o'chir
            @CacheEvict(value = "products-category", allEntries = true), // Jamoaviy keshni portlat
            @CacheEvict(value = "products-search", allEntries = true)

    })
    public void delete(UUID id) {
        Product product = findProductByIdForAdmin(id); // <--- Qayta foydalanamiz
        product.setDeleted(true);
        product.setActive(false);
        // productRepository.save(product); // Shart emas, lekin tushunarliroq
        log.info("Product soft-deleted with ID: {}", id);
    }


    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "#id")
    public ProductDto.ProductResponse getByIdForAdmin(UUID id) {
        return productMapper.toResponse(findProductByIdForAdmin(id));
    }


    @Override
    @Transactional(readOnly = true)
    public Page<ProductDto.ProductResponse> getAllForAdmin(Pageable pageable) {
        log.debug("Fetching all products for Admin panel. Page: {}", pageable.getPageNumber());

        return productRepository.findAllByDeletedFalse(pageable)
                .map(productMapper::toResponse);
    }


    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "products", key = "#id"), // Shaxsiy keshni o'chir
            @CacheEvict(value = "products-category", allEntries = true), // Jamoaviy keshni portlat
            @CacheEvict(value = "products-search", allEntries = true)
    })
    public void updatePrice(UUID id, BigDecimal price) {
        log.info("Updating price for product {}: new price {}", id, price);
        Product product = findProductByIdForAdmin(id);
        product.setPrice(price);
        productRepository.save(product);
    }


    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "products", key = "#id"), // Shaxsiy keshni o'chir
            @CacheEvict(value = "products-category", allEntries = true),// Jamoaviy keshni portlat
            @CacheEvict(value = "products-search", allEntries = true)
    })
    public void changeStatus(UUID id, boolean active) {
        log.info("Changing status for product {}: active={}", id, active);
        Product product = findProductByIdForAdmin(id);
        product.setActive(active);
        productRepository.save(product);
    }


    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "products-category", key = "#categoryId + '-p' + #page")
    public List<ProductDto.ProductShortResponse> getProductsByCategoryId(UUID categoryId, int page) {
        // Sahifani 5 tadan qilib chegaralash
        Pageable limitFive = PageRequest.of(page, 5);

        System.out.println("---------------------------------------------------------");
        System.out.println("DEBUG: [DATABASE] Kategoriya bo'yicha bazadan olinyapti...");
        System.out.println("DEBUG: CategoryID: " + categoryId + " | Page: " + page);
        System.out.println("---------------------------------------------------------");

        // Repository endi List qaytaradi, lekin Pageable orqali limitni boshqaradi
        return productRepository.findByCategoryIdCustom(categoryId, limitFive)
                .stream()
                .map(productMapper::toResponseForUser)
                .toList();
    }


    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "#id")
    public ProductDto.ProductShortResponse getById(UUID id) {
        Product product = findProductByIdForUser(id);
        return productMapper.toResponseForUser(product);
    }


    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "products-search", key = "#name.trim().toLowerCase() + '-p' + #page")
    public List<ProductDto.ProductShortResponse> searchByName(String name, int page) {
        Pageable limitFive = PageRequest.of(page, 5);
        String cleanName = name.trim();

        System.out.println("=========================================================");
        System.out.println("DEBUG: [SEARCH-DB] Qidiruv bazadan bajarilmoqda...");
        System.out.println("DEBUG: Name: " + cleanName + " | Page: " + page);
        System.out.println("=========================================================");

        return productRepository.searchByNameCustom(cleanName, limitFive)
                .stream()
                .map(productMapper::toResponseForUser)
                .toList();
    }


    @Override
    @Transactional
    @CacheEvict(value = {"products", "products-category", "products-search"}, allEntries = true)
    public void removePromotionReference(UUID id) {
        productRepository.detachPromotionFromProducts(id);
    }


    @Override
    @Transactional
    @CacheEvict(value = {"products", "products-category", "products-search"}, allEntries = true)
    public void assignPromotionToProducts(Promotion promotion, List<UUID> productIds) {
        // 1. Faqat kerakli (active va o'chirilmagan) mahsulotlarni bazadan olamiz
        List<Product> products = productRepository.findMassiveProducts(productIds);

        if (products.isEmpty()) {
            throw new RecordNotFoundException("Berilgan IDlar bo'yicha birorta ham faol mahsulot topilmadi");
        }

        // 2. Har bir mahsulotga aksiyani biriktiramiz
        products.forEach(product -> product.setPromotion(promotion));

        // 3. Ommaviy saqlaymiz
        productRepository.saveAll(products);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDto.ProductResponse> getProductsByPromotionId(UUID promotionId, Pageable pageable) {
        return productRepository.findAllByPromotionIdAndDeletedFalse(promotionId, pageable)
                .map(productMapper::toResponse);
    }


    public Product findProductByIdForUser(UUID id) {
        return productRepository.findByIdAndDeletedFalseAndActiveIsTrue(id)
                .orElseThrow(()-> new RecordNotFoundException("Product with id " + id + " not found"));
    }


    private Product findProductByIdForAdmin(UUID id) {
        return productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RecordNotFoundException("Mahsulot topilmadi"));
    }


    private void validateProductUniqueness(String name) {
        if (productRepository.existsByNameAndDeletedFalse(name)) {
            throw new RecordAlreadyException("Bunday nomli mahsulot mavjud: " + name);
        }
    }


    private List<Category> validateAndGetCategories(List<UUID> categoryIds) {
        List<Category> categories = categoryService.getCategoriesByIdInAndIsDeletedFalse(categoryIds);

        if (categories.size() != categoryIds.size()) {
            log.error("Kategoriyalar soni mos kelmadi. So'ralgan: {}, Topilgan: {}", categoryIds.size(), categories.size());
            throw new RecordNotFoundException("Ba'zi kategoriyalar topilmadi yoki o'chirilgan!");
        }
        return categories;
    }


    private void handleProductImage(Product product, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Mahsulot rasmi yuklanishi shart!");
        }
        String fileName = "products" + "/" + UUID.randomUUID() + ".jpg";
        String imageUrl = fileService.uploadFile(file, fileName);
        product.setImageUrl(imageUrl);
    }
}
