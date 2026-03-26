package com.example.food.mapper;


import com.example.food.dto.ChatMessageDto;
import com.example.food.entity.ChatMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(config = MapStructConfig.class)
public interface ChatMapper {

    // Request Record -> Entity (BaseEntity maydonlarini inobatga olgan holda)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "status", ignore = true), // Service qatlamida SET qilinadi
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),
            @Mapping(target = "deletedAt", ignore = true),
            @Mapping(target = "deleted", ignore = true)
    })
    ChatMessage toEntity(ChatMessageDto.ChatMessageRequest request);

    // Entity -> Response Record
    // Record ichidagi maydon nomlari Entity bilan bir xil bo'lsa, avtomatik map bo'ladi
    ChatMessageDto.ChatMessageResponse toResponse(ChatMessage entity);
}
