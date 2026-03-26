package com.example.food.mapper;

import com.example.food.dto.LocationDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.time.LocalDateTime;

@Mapper(config = MapStructConfig.class, imports = {LocalDateTime.class})
public interface LocationMapper {

    /**
     * CourierLocationRequest -> LocationResponse
     * latitude, longitude va bearing nomlari bir xil bo'lgani uchun avtomat ulanadi.
     * updatedAt maydoniga esa joriy vaqtni (now) yozib yuboramiz.
     */
    @Mapping(target = "updatedAt", expression = "java(LocalDateTime.now())")
    LocationDto.LocationResponse toResponse(LocationDto.CourierLocationRequest request);
}
