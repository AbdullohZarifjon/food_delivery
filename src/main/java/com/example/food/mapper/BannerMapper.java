package com.example.food.mapper;

import com.example.food.dto.BannerDto;
import com.example.food.entity.Banner;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface BannerMapper {

    /**
     * Request DTO dan Entity ga o'girish.
     * imageUrl Request ichida yo'q, uni service'da fayl yuklangandan keyin o'zimiz set qilamiz.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "active", ignore = true) // default true bo'ladi Entity darajasida
    Banner toEntity(BannerDto.BannerRequest bannerRequest);

    /**
     * Entity dan Response DTO ga o'girish.
     */
    BannerDto.BannerResponse toResponse(Banner banner);
}