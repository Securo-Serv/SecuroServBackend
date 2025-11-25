package com.example.SecuroServBackend.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
public class FileEntity {

    @Id
    @GeneratedValue
    @org.hibernate.annotations.UuidGenerator
    private UUID fileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    private Folder folder;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column()
    private String fileName;

    @Column()
    private String localFilePath;

    @Column(length = 500)
    private String encryptedFileKeyData;

    @Column(length = 64)
    private String encryptedFileKeyIv;

    @Column(length = 64)
    private String encryptedFileKeyTag;

    @Column(length = 64)
    private String iv;

    @Column(length = 64)
    private String tag;

    @Column()
    private Long fileSizeBytes;

    @Column()
    private LocalDateTime uploadedAt = LocalDateTime.now();
}
