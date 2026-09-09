package com.campuslfp.dto.request;

import com.campuslfp.enums.ItemStatus;
import com.campuslfp.enums.ItemType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemUpdateRequest {
    private String title;
    private String description;
    private ItemType type;
    private String category;
    private String tags;
    private String location;
    private String color;
    private ItemStatus status;
    private Boolean matched;
    private Boolean flagged;
}
