package com.campuslfp.dto;

import com.campuslfp.model.ItemStatus;
import com.campuslfp.model.ItemType;
import lombok.Data;

@Data
public class ItemDto {
    private String title;
    private String description;
    private ItemType type;
    private String category;
    private String tags;
    private String location;
    private String color;
    private String imageUrl;
    private ItemStatus status;
    // Admins can set this to mark an item as matched to another
    private Boolean matched;
    // Admins may also clear/set flagged via admin update; regular users flag via dedicated endpoint
    private Boolean flagged;
}
