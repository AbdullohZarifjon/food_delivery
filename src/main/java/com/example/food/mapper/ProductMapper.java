package com.example.food.mapper;

import com.example.food.dto.ProductDto;
import com.example.food.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface ProductMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "promotion", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    Product toEntity(ProductDto.ProductRequest productRequest);

    @Mapping(target = "currentPrice", expression = "java(product.calculateCurrentPrice())")
    ProductDto.ProductResponse toResponse(Product product);

    @Mapping(target = "currentPrice", expression = "java(product.calculateCurrentPrice())")
    ProductDto.ProductShortResponse toResponseForUser(Product product);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "promotion", ignore = true)
    @Mapping(target = "active", ignore = true)
    void updateEntity(@MappingTarget Product product, ProductDto.ProductRequest productRequest);


}
