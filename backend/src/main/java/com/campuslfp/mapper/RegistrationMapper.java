package com.campuslfp.mapper;

import com.campuslfp.dto.request.RegisterRequest;
import com.campuslfp.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RegistrationMapper {

    User toEntity(RegisterRequest request);
}
