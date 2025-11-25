package com.example.SecuroServBackend.Mappers;

import com.example.SecuroServBackend.DTOs.AuthUserDTO;
import com.example.SecuroServBackend.Entity.AuthUser;

import java.util.UUID;

public class AuthUserMapper {
    public static AuthUserDTO toDto(AuthUser user) {
        if (user == null) {
            return null;
        }

        AuthUserDTO dto = new AuthUserDTO();
        dto.setEmail(user.getEmail());
        dto.setUsername(user.getUsername());
        dto.setPassword(user.getPassword());
        return dto;
    }

    public static AuthUser toEntity(AuthUserDTO dto) {
        if (dto == null) {
            return null;
        }

        AuthUser user = new AuthUser();

        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());

        user.setUsername(dto.getEmail());
        return user;
    }
}
