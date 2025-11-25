package com.example.SecuroServBackend.Controllers;

import com.example.SecuroServBackend.Configuration.JwtUtil;
import com.example.SecuroServBackend.DTOs.AuthUserDTO;
import com.example.SecuroServBackend.Entity.AuthUser;
import com.example.SecuroServBackend.Entity.FileEntity;
import com.example.SecuroServBackend.Entity.User;
import com.example.SecuroServBackend.Repository.AuthUserRepo;
import com.example.SecuroServBackend.Repository.FileEntityRepo;
import com.example.SecuroServBackend.Service.FileEntityServices;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;


@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileEntityRepo fileEntityRepo;
    private final JwtUtil jwtUtil;
    private final AuthUserRepo authRepository;

    @DeleteMapping("/{fileId}")
    public Map<String, String> deleteFile(@RequestHeader("Authorization") String token,
                                          @PathVariable UUID fileId) {
        String jwtToken = token.replace("Bearer ", "");
        String email = jwtUtil.extractUsername(jwtToken);

        AuthUser authUser = authRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        User user = authUser.getUser();
        UUID userId = user.getUserId();

        Optional<FileEntity> fileOpt = fileEntityRepo.findById(fileId);
        if (fileOpt.isEmpty()) {
            return Map.of("message", "File not found");
        }

        FileEntity fileEntity = fileOpt.get();


        if (!fileEntity.getUser().getUserId().equals(userId)) {
            return Map.of("error", "Unauthorized to delete this file");
        }


        File physicalFile = new File(fileEntity.getLocalFilePath());
        if (physicalFile.exists()) {
            if (!physicalFile.delete()) {
                return Map.of("error", "Failed to delete file from storage");
            }
        }


        fileEntityRepo.delete(fileEntity);

        return Map.of("message", "File deleted successfully");
    }
}
