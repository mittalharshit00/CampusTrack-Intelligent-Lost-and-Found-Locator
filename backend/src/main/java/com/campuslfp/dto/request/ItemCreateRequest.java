package com.campuslfp.dto.request;

import com.campuslfp.enums.ItemType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemCreateRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Item type is required")
    private ItemType type;

    private String category;
    private String tags;

    @NotBlank(message = "Location is required")
    private String location;

    private String color;
}
