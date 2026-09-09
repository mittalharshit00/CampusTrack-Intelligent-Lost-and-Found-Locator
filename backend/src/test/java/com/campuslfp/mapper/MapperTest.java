package com.campuslfp.mapper;

import com.campuslfp.dto.response.AuthResponse;
import com.campuslfp.dto.response.ItemDto;
import com.campuslfp.enums.ItemStatus;
import com.campuslfp.enums.ItemType;
import com.campuslfp.enums.Role;
import com.campuslfp.model.Item;
import com.campuslfp.model.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class MapperTest {

    private final ItemMapper itemMapper = Mappers.getMapper(ItemMapper.class);
    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @Test
    void itemMapperShouldConvertDtoToEntity() {
        ItemDto dto = new ItemDto();
        dto.setTitle("Wallet");
        dto.setDescription("Black leather wallet");
        dto.setType(ItemType.LOST);
        dto.setCategory("Accessories");
        dto.setTags("wallet, leather");
        dto.setLocation("Library");
        dto.setColor("Black");
        dto.setStatus(ItemStatus.OPEN);

        User user = new User();
        user.setId(10L);
        user.setEmail("student@college.edu");

        Item entity = itemMapper.toEntity(dto);
        entity.setPostedBy(user);

        assertNotNull(entity);
        assertEquals("Wallet", entity.getTitle());
        assertEquals(ItemType.LOST, entity.getType());
        assertEquals("Library", entity.getLocation());
        assertEquals(user, entity.getPostedBy());
        assertEquals(ItemStatus.OPEN, entity.getStatus());
    }

    @Test
    void userMapperShouldConvertUserToAuthResponse() {
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@college.edu");
        user.setRole(Role.ROLE_STUDENT);
        user.setVerified(true);
        user.setApproved(true);
        user.setCreatedAt(Instant.now());

        AuthResponse response = userMapper.toAuthResponse(user);
        response.setToken("jwt-token");

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("Test User", response.getName());
        assertEquals("test@college.edu", response.getEmail());
        assertEquals(Role.ROLE_STUDENT, response.getRole());
        assertTrue(response.isVerified());
        assertTrue(response.isApproved());
    }
}
