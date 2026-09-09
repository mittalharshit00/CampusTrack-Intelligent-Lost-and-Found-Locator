package com.campuslfp.mapper;

import com.campuslfp.dto.request.ItemCreateRequest;
import com.campuslfp.dto.request.ItemUpdateRequest;
import com.campuslfp.dto.response.ItemDto;
import com.campuslfp.model.Item;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateReported", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "matched", ignore = true)
    @Mapping(target = "flagged", ignore = true)
    @Mapping(target = "postedBy", ignore = true)
    @Mapping(target = "title", source = "title")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "category", source = "category")
    @Mapping(target = "tags", source = "tags")
    @Mapping(target = "location", source = "location")
    @Mapping(target = "color", source = "color")
    @Mapping(target = "imageUrl", ignore = true)
    ItemDto toDto(ItemCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateReported", ignore = true)
    @Mapping(target = "status", source = "status")
    @Mapping(target = "matched", source = "matched")
    @Mapping(target = "flagged", source = "flagged")
    @Mapping(target = "postedBy", ignore = true)
    @Mapping(target = "title", source = "title")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "category", source = "category")
    @Mapping(target = "tags", source = "tags")
    @Mapping(target = "location", source = "location")
    @Mapping(target = "color", source = "color")
    @Mapping(target = "imageUrl", ignore = true)
    ItemDto toDto(ItemUpdateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateReported", ignore = true)
    @Mapping(target = "status", source = "status")
    @Mapping(target = "matched", source = "matched")
    @Mapping(target = "flagged", source = "flagged")
    @Mapping(target = "postedBy", ignore = true)
    @Mapping(target = "title", source = "title")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "category", source = "category")
    @Mapping(target = "tags", source = "tags")
    @Mapping(target = "location", source = "location")
    @Mapping(target = "color", source = "color")
    @Mapping(target = "imageUrl", source = "imageUrl")
    Item toEntity(ItemDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateReported", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "matched", ignore = true)
    @Mapping(target = "flagged", ignore = true)
    @Mapping(target = "postedBy", ignore = true)
    @Mapping(target = "title", source = "title")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "category", source = "category")
    @Mapping(target = "tags", source = "tags")
    @Mapping(target = "location", source = "location")
    @Mapping(target = "color", source = "color")
    @Mapping(target = "imageUrl", source = "imageUrl")
    void updateUserFields(@MappingTarget Item item, ItemDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateReported", ignore = true)
    @Mapping(target = "status", source = "status")
    @Mapping(target = "matched", source = "matched")
    @Mapping(target = "flagged", source = "flagged")
    @Mapping(target = "postedBy", ignore = true)
    @Mapping(target = "title", source = "title")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "category", source = "category")
    @Mapping(target = "tags", source = "tags")
    @Mapping(target = "location", source = "location")
    @Mapping(target = "color", source = "color")
    @Mapping(target = "imageUrl", source = "imageUrl")
    void updateAdminFields(@MappingTarget Item item, ItemDto dto);
}
