package com.example.SecuroServBackend.Controllers;

import com.example.SecuroServBackend.Entity.FileEntity;
import com.example.SecuroServBackend.Repository.FileEntityRepo;
import com.example.SecuroServBackend.Repository.FolderRepo;
import com.example.SecuroServBackend.Service.VaultDecryptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/vault")
@RequiredArgsConstructor
public class VaultDecryptionController {

    private final VaultDecryptionService vaultDecryptionService;
    private final FileEntityRepo fileEntityRepo;
    private final FolderRepo folderRepo;
    @GetMapping("/decrypt/{fileId}")
    public ResponseEntity<?> decryptSingleFile(
            @RequestHeader("Authorization") String token,
            @PathVariable UUID fileId
    ) {
        try {
            String jwtToken = token.replace("Bearer ", "");

            // ✅ Check if it's a file
            if (fileEntityRepo.findById(fileId).isPresent()) {
                FileEntity file = fileEntityRepo.findById(fileId).get();
                byte[] decryptedBytes = vaultDecryptionService.decryptSingleFileToBytes(jwtToken, fileId);

                String originalFileName = file.getFileName();
                if (originalFileName.toLowerCase().endsWith(".enc")) {
                    originalFileName = originalFileName.substring(0, originalFileName.length() - 4);
                }

                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + originalFileName + "\"")
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .body(decryptedBytes);
            }

            // ✅ If it's a folder → use existing folder decrypt logic (which zips output)
            else if (folderRepo.findById(fileId).isPresent()) {
                Path decryptedFolderPath = vaultDecryptionService.decryptFolderRecursively(jwtToken, fileId);

                // Compress to ZIP in memory (optional optimization)
                Path zipPath = Files.createTempFile("decrypted_", ".zip");
                try (FileOutputStream fos = new FileOutputStream(zipPath.toFile());
                     java.util.zip.ZipOutputStream zipOut = new java.util.zip.ZipOutputStream(fos)) {

                    Files.walk(decryptedFolderPath)
                            .filter(path -> !Files.isDirectory(path))
                            .forEach(path -> {
                                try {
                                    String zipEntryName = decryptedFolderPath.relativize(path).toString();
                                    zipOut.putNextEntry(new java.util.zip.ZipEntry(zipEntryName));
                                    Files.copy(path, zipOut);
                                    zipOut.closeEntry();
                                } catch (Exception e) {
                                    throw new RuntimeException("Error zipping file: " + path, e);
                                }
                            });
                }

                byte[] zipBytes = Files.readAllBytes(zipPath);
                String zipFileName = decryptedFolderPath.getFileName().toString() + ".zip";

                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + zipFileName + "\"")
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .body(zipBytes);
            }

            return ResponseEntity.badRequest().body(" Invalid ID — no file or folder found with this ID");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(" Decryption failed: " + e.getMessage());
        }
    }

    @GetMapping("/decrypt-folder/{folderId}")
    public ResponseEntity<?> decryptEntireFolder(
            @RequestHeader("Authorization") String token,
            @PathVariable UUID folderId
    ) {
        try {
            // Step 1: Decrypt folder and get its path
            Path decryptedFolderPath = vaultDecryptionService.decryptFolderRecursively(token.replace("Bearer ", ""), folderId);

            // Step 2: Create ZIP of decrypted folder
            ByteArrayOutputStream zipOutputStream = new ByteArrayOutputStream();
            try (ZipOutputStream zos = new ZipOutputStream(zipOutputStream)) {
                Files.walk(decryptedFolderPath)
                        .filter(Files::isRegularFile)
                        .forEach(filePath -> {
                            try (InputStream fis = Files.newInputStream(filePath)) {
                                ZipEntry zipEntry = new ZipEntry(decryptedFolderPath.relativize(filePath).toString());
                                zos.putNextEntry(zipEntry);
                                StreamUtils.copy(fis, zos);
                                zos.closeEntry();
                            } catch (IOException e) {
                                e.printStackTrace(); // Optional: log or skip
                            }
                        });
            }

            // Step 3: Send ZIP as response
            byte[] zipBytes = zipOutputStream.toByteArray();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"decrypted_folder.zip\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(zipBytes);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(" Folder decryption failed: " + e.getMessage());
        }

    }

}
