package com.example.SecuroServBackend.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Data
public class Folder {
    @Id
    @GeneratedValue
    @org.hibernate.annotations.UuidGenerator
    private UUID folderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_folder_id")
    private Folder parentFolder;

    @OneToMany(mappedBy = "parentFolder", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Folder> subFolders = new HashSet<>();

    @OneToMany(mappedBy = "folder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FileEntity> files = new ArrayList<>();

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String encryptedFolderKeyData;

    @Column(length = 64)
    private String encryptedFolderKeyIv;

    @Column(length = 64)
    private String encryptedFolderKeyTag;

    @Column
    private String localFolderPath;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}


