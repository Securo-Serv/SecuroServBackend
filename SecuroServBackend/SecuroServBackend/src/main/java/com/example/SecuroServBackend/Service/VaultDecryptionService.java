package com.example.SecuroServBackend.Service;

import com.example.SecuroServBackend.Entity.FileEntity;
import com.example.SecuroServBackend.Entity.Folder;
import com.example.SecuroServBackend.Repository.FileEntityRepo;
import com.example.SecuroServBackend.Repository.FolderRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class VaultDecryptionService {
    private final VaultSessionCache vaultSessionCache;
    private final FileEntityRepo fileRepository;
    private final FolderRepo folderRepository;

    private static final String ALGO = "AES/GCM/NoPadding";

    @Transactional
    public byte[] decryptSingleFileToBytes(String jwtToken, UUID fileId) throws Exception {
        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        SecretKeySpec rootKey = vaultSessionCache.getRootKey(jwtToken);
        if (rootKey == null)
            throw new RuntimeException("Root key not found — please login again.");


        SecretKeySpec parentKey = (file.getFolder() == null)
                ? rootKey
                : decryptNestedFolderKey(file.getFolder(), rootKey);


        Cipher keyCipher = Cipher.getInstance("AES/GCM/NoPadding");
        keyCipher.init(Cipher.DECRYPT_MODE, parentKey,
                new GCMParameterSpec(128, Base64.getDecoder().decode(file.getEncryptedFileKeyIv())));

        byte[] decodedKeyData = Base64.getDecoder().decode(file.getEncryptedFileKeyData());
        byte[] fileKeyBytes = keyCipher.doFinal(decodedKeyData);
        SecretKeySpec fileKey = new SecretKeySpec(fileKeyBytes, "AES");


        Cipher fileCipher = Cipher.getInstance("AES/GCM/NoPadding");
        fileCipher.init(Cipher.DECRYPT_MODE, fileKey,
                new GCMParameterSpec(128, Base64.getDecoder().decode(file.getIv())));

        Path encryptedFilePath = Paths.get(file.getLocalFilePath());

        try (ByteArrayOutputStream decryptedOutput = new ByteArrayOutputStream();
             FileInputStream fis = new FileInputStream(encryptedFilePath.toFile());
             CipherInputStream cis = new CipherInputStream(fis, fileCipher)) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = cis.read(buffer)) != -1) {
                decryptedOutput.write(buffer, 0, bytesRead);
            }

            return decryptedOutput.toByteArray();
        }
    }

    public Path decryptFolderRecursively(String jwtToken, UUID folderId) throws Exception {
        System.out.println("👕👕👕 decryption logic ");

        Folder folder = folderRepository.findWithAllData(folderId);
        if (folder == null) {
            throw new RuntimeException("Folder not found");
        }

        System.out.println("🧩 Folder: " + folder.getName() + " has " +
                folder.getFiles().size() + " files and " +
                folder.getSubFolders().size() + " subfolders");

        SecretKeySpec rootKey = vaultSessionCache.getRootKey(jwtToken);
        if (rootKey == null)
            throw new RuntimeException("Root key not found — please login again.");


        SecretKeySpec folderKey = (folder.getParentFolder() == null)
                ? decryptFolderKey(folder, rootKey)
                : decryptNestedFolderKey(folder, rootKey);

        Path folderPath = Paths.get(folder.getLocalFolderPath());


        for (FileEntity file : folder.getFiles()) {
            decryptFileWithKey(file, folderKey);
        }


        for (Folder subfolder : folder.getSubFolders()) {
            decryptFolderRecursively(jwtToken, subfolder.getFolderId());
        }
        System.out.println("🧩 Folder: " + folder.getName() + " has " + folder.getFiles().size() + " files");

        System.out.println("✅ Folder decrypted successfully: " + folderPath);
        return folderPath;
    }


    private void decryptFileWithKey(FileEntity fileEntity, SecretKeySpec folderKey) throws Exception {
        System.out.println("🤡🤡🤡🤡 this decryption file with key ");
        Cipher keyCipher = Cipher.getInstance(ALGO);
        keyCipher.init(
                Cipher.DECRYPT_MODE,
                folderKey,
                new GCMParameterSpec(128, Base64.getDecoder().decode(fileEntity.getEncryptedFileKeyIv()))
        );
        byte[] fileKeyBytes = keyCipher.doFinal(Base64.getDecoder().decode(fileEntity.getEncryptedFileKeyData()));
        SecretKeySpec fileKey = new SecretKeySpec(fileKeyBytes, "AES");

        Cipher fileCipher = Cipher.getInstance(ALGO);
        fileCipher.init(
                Cipher.DECRYPT_MODE,
                fileKey,
                new GCMParameterSpec(128, Base64.getDecoder().decode(fileEntity.getIv()))
        );

        Path encryptedFilePath = Paths.get(fileEntity.getLocalFilePath());
        Path decryptedFilePath = Paths.get(encryptedFilePath.toString().replace(".enc", ".dec"));

        try (FileInputStream fis = new FileInputStream(encryptedFilePath.toFile());
             FileOutputStream fos = new FileOutputStream(decryptedFilePath.toFile());
             CipherInputStream cis = new CipherInputStream(fis, fileCipher)) {
            cis.transferTo(fos);
        }

        System.out.println("   ↳ File decrypted: " + fileEntity.getFileName());
    }

    private SecretKeySpec decryptFolderKey(Folder folder, SecretKeySpec parentKey) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGO);
        cipher.init(
                Cipher.DECRYPT_MODE,
                parentKey,
                new GCMParameterSpec(128, Base64.getDecoder().decode(folder.getEncryptedFolderKeyIv()))
        );
        byte[] folderKeyBytes = cipher.doFinal(Base64.getDecoder().decode(folder.getEncryptedFolderKeyData()));
        return new SecretKeySpec(folderKeyBytes, "AES");
    }


    private SecretKeySpec decryptNestedFolderKey(Folder folder, SecretKeySpec rootKey) throws Exception {
        if (folder.getParentFolder() == null)
            return decryptFolderKey(folder, rootKey);
        SecretKeySpec parentKey = decryptNestedFolderKey(folder.getParentFolder(), rootKey);
        return decryptFolderKey(folder, parentKey);
    }
}

