package com.campuslfp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationCreateRequest {

    @NotNull(message = "Item id is required")
    private Long itemId;

    @NotBlank(message = "Other user email is required")
    private String otherUserEmail;
}
