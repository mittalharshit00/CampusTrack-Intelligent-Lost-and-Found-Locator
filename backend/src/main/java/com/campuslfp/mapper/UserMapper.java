package com.campuslfp.mapper;

import com.campuslfp.dto.response.AuthResponse;
import com.campuslfp.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    AuthResponse toAuthResponse(User user);
}
