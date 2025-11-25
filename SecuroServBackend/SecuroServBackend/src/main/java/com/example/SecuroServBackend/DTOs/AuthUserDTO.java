package com.example.SecuroServBackend.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthUserDTO {
    private UUID AuthUserID;
    private String email;
    private String username;
    private String password;
    private String role;
}
