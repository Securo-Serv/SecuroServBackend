package com.example.SecuroServBackend.Service;

import com.example.SecuroServBackend.DTOs.FileEntityDTO;
import com.example.SecuroServBackend.Entity.FileEntity;
import com.example.SecuroServBackend.Entity.Role;
import com.example.SecuroServBackend.Entity.User;
import com.example.SecuroServBackend.Repository.FileEntityRepo;
import com.example.SecuroServBackend.Repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileEntityServices {

    private final FileEntityRepo fileEntityRepo;
    private final UserRepo userRepo;

    public FileEntity uploadFile(UUID id) {
        FileEntity fileEntity = fileEntityRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found!"));

        FileEntityDTO fileEntityDTO = new FileEntityDTO();
        fileEntityDTO.setFileId(fileEntity.getFileId());
        fileEntityDTO.setName(fileEntity.getFileName());
        fileEntityDTO.setCreationAT(LocalDateTime.from(fileEntity.getUploadedAt()));
        fileEntityDTO.setType("file");

        return fileEntity;
    }


    private static final long USER_STORAGE_LIMIT = 30L * 1024 * 1024 * 1024 ;   // 30 GB
    private static final long PREMIUM_STORAGE_LIMIT = 100L * 1024 * 1024 * 1024 ; // 100 GB


    public long calculateUsedStorage(User user) {
        return fileEntityRepo.getTotalStorageUsedByUser(user.getUserId());
    }

    public long getMaxStorage(User user) {
        return user.getRole() == Role.PREMIUM ? PREMIUM_STORAGE_LIMIT : USER_STORAGE_LIMIT;
    }

    public boolean canUpload(User user, long newFileSize) {
        long used = calculateUsedStorage(user);
        return used + newFileSize <= getMaxStorage(user);
    }

    public FileEntity saveFile(UUID userId, MultipartFile file) throws IOException {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        long fileSize = file.getSize(); // bytes

        if (!canUpload(user, fileSize)) {
            throw new RuntimeException("❌ Storage limit reached. Upgrade to Premium for 100GB.");
        }

        FileEntity fileEntity = new FileEntity();
        fileEntity.setUser(user);
        fileEntity.setFileName(file.getOriginalFilename());
        fileEntity.setFileSizeBytes(fileSize);
        fileEntity.setUploadedAt(LocalDateTime.now());
        fileEntity.setLocalFilePath("/vault/user_" + user.getUserId() + "/" + file.getOriginalFilename());


        return fileEntityRepo.save(fileEntity);
    }
}
