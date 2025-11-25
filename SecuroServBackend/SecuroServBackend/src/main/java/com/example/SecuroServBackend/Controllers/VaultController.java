package com.example.SecuroServBackend.Controllers;

import com.example.SecuroServBackend.Configuration.JwtUtil;
import com.example.SecuroServBackend.DTOs.FileEntityDTO;
import com.example.SecuroServBackend.DTOs.FolderDTO;
import com.example.SecuroServBackend.Entity.AuthUser;
import com.example.SecuroServBackend.Entity.FileEntity;
import com.example.SecuroServBackend.Entity.Folder;
import com.example.SecuroServBackend.Entity.User;
import com.example.SecuroServBackend.Repository.AuthUserRepo;
import com.example.SecuroServBackend.Repository.UserRepo;
import com.example.SecuroServBackend.Service.FileEntityServices;
import com.example.SecuroServBackend.Service.FolderServices;
import com.example.SecuroServBackend.Service.VaultEncryptionService;
import jakarta.servlet.ServletOutputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/encryption")
@RequiredArgsConstructor
public class VaultController {
    private final FileEntityServices fileEntityServices;
    private final FolderServices folderServices;
    private final VaultEncryptionService vaultEncryptionService;
    private final AuthUserRepo authrepository;
    private final JwtUtil jwtUtil;

    private static final String BASE_STORAGE_PATH = "D:/SecuroServ_Storage";

    @PostMapping("/upload")
    public ResponseEntity<?> uploadAndEncryptFiles(
            @RequestHeader("Authorization") String token,
            @RequestParam MultipartFile files
    ) {
        try {

            String jwtToken = token.replace("Bearer ", "");
            String email = jwtUtil.extractUsername(jwtToken);

            AuthUser authUser = authrepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            User user = authUser.getUser();


            long fileSize = files.getSize();
            long usedStorage = fileEntityServices.calculateUsedStorage(user);
            long maxStorage = fileEntityServices.getMaxStorage(user);

            System.out.println("📊 Used: " + usedStorage + " / Max: " + maxStorage + " / File: " + fileSize);

            if (usedStorage + fileSize > maxStorage) {
                return ResponseEntity.status(403)
                        .body(" Storage limit exceeded. Please upgrade to Premium for 100 GB.");
            }


            Path userFolderPath = Paths.get(BASE_STORAGE_PATH, "user_" + user.getUserId());
            Files.createDirectories(userFolderPath);


            FileEntity fileEntity = vaultEncryptionService.encryptSingleFile(jwtToken, files, user, userFolderPath);

            FileEntityDTO fileEntityDTO = new FileEntityDTO();
            fileEntityDTO.setFileId(fileEntity.getFileId());
            fileEntityDTO.setName(fileEntity.getFileName());
            fileEntityDTO.setCreationAT(fileEntity.getUploadedAt());
            fileEntityDTO.setType("file");

            return ResponseEntity.ok(fileEntityDTO);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(" Upload failed: " + e.getMessage());
        }
    }



    @PostMapping("/upload-folder")
    public ResponseEntity<?> uploadAndEncryptFolder(
            @RequestHeader("Authorization") String token,
            @RequestParam("file") List<MultipartFile> files,
            @RequestParam(value = "relativePath", required = false) List<String> relativePaths
    ) {
        try {

            String jwtToken = token.replace("Bearer ", "");
            String email = jwtUtil.extractUsername(jwtToken);
            AuthUser authUser = authrepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            User user = authUser.getUser();


            Path userVaultBase = Paths.get(BASE_STORAGE_PATH, "user_" + user.getUserId());
            Files.createDirectories(userVaultBase);

            FolderDTO folderDTO = new FolderDTO();
            boolean firstFolderProcessed = false;


            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                String relativePath = (relativePaths != null && relativePaths.size() > i)
                        ? relativePaths.get(i)
                        : "";


                if (!firstFolderProcessed && relativePath != null && !relativePath.isBlank()) {
                    Path relPathObj = Paths.get(relativePath);
                    String rootFolderName = relPathObj.getName(0).toString(); // e.g. "MyFolder"
                    Path folderFullPath = userVaultBase.resolve(rootFolderName).normalize();


                    folderDTO.setName(rootFolderName);
                    folderDTO.setType("Folder");
                    folderDTO.setCreationAT(LocalDateTime.now());

                    System.out.println(" Encrypting folder recursively: " + folderFullPath);


                    vaultEncryptionService.encryptAndStoreFolderRecursively(jwtToken, folderFullPath, null, user);

                    firstFolderProcessed = true;
                }


                Path targetFolder = userVaultBase;
                if (relativePath != null && !relativePath.isBlank()) {
                    Path parentPath = Paths.get(relativePath).getParent();
                    if (parentPath != null) {
                        targetFolder = userVaultBase.resolve(parentPath).normalize();
                        Files.createDirectories(targetFolder);
                    }
                }


                vaultEncryptionService.encryptSingleFile(jwtToken, file, user, targetFolder);
            }


            return ResponseEntity.ok(folderDTO);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(" Folder upload failed: " + e.getMessage());
        }
    }

    @GetMapping("/vault/files")
    public ResponseEntity<?> getFile(@RequestParam UUID Id){
        System.out.println("thhisisiisisisi apaaappppiii iss callledd");
        return ResponseEntity.ok(fileEntityServices.uploadFile(Id));
    }

    @GetMapping("/vault/folders")
    public ResponseEntity<?> getFolder(@RequestParam UUID Id){
        return ResponseEntity.ok(folderServices);
    }

}






