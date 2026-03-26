package com.example.food.mapper;

import com.example.food.dto.CategoryDto;
import com.example.food.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface CategoryMapper {

    /**
     * Request -> Entity
     * imageUrl va products Service qatlamida alohida qo'shilgani uchun ularni ignore qilamiz.
     * BaseEntity maydonlari ham avtomat ignore bo'ladi.
     */
    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "active", constant = "true") // Yangi kategoriya doim aktiv bo'lib yaratilsin desak
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    Category toEntity(CategoryDto.CategoryRequest categoryRequest);

    /**
     * Entity -> Response
     * Agar maydon nomlari bir xil bo'lsa (name, sortOrder, imageUrl),
     * MapStruct ularni o'zi avtomat ulab oladi.
     */
    CategoryDto.CategoryResponse toResponse(Category category);

    /**
     * UpdateRequest -> Mavjud Entity-ni yangilash
     * Bu metod bazadan olingan ob'ektni ustiga yangi ma'lumotlarni yozish uchun kerak.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "products", ignore = true)
    void updateEntity(CategoryDto.UpdateCategoryRequest request, @MappingTarget Category category);
}
