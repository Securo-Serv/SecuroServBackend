package com.example.SecuroServBackend.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthUser {
    @Id
    @GeneratedValue
    @org.hibernate.annotations.UuidGenerator
    private UUID AuthUserID;
    private String username;
    private String email;
    private String password;
    private String role;

    @Column(length = 64)
    private String salt;

    @Column()
    private Integer iterations = 250000;

    @Column(length = 500)
    private String encryptedRootKeyData;

    @Column(length = 64)
    private String encryptedRootKeyIv;

    @Column(length = 64)
    private String encryptedRootKeyTag;


    @OneToOne(mappedBy = "authUser", cascade = CascadeType.ALL)
    private User user;
}
