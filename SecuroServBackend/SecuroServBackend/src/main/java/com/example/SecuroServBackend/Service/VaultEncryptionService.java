package com.example.SecuroServBackend.Service;

import com.example.SecuroServBackend.Entity.FileEntity;
import com.example.SecuroServBackend.Entity.Folder;
import com.example.SecuroServBackend.Entity.User;
import com.example.SecuroServBackend.Repository.FileEntityRepo;
import com.example.SecuroServBackend.Repository.FolderRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;
import java.util.stream.Stream;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;


@Service
@RequiredArgsConstructor
public class VaultEncryptionService {

    @PersistenceContext
    private EntityManager entityManager;


    private final VaultSessionCache vaultSessionCache;
    private final FolderRepo folderRepository;
    private final FileEntityRepo fileRepository;


    private static final String ALGO = "AES/GCM/NoPadding";
    private static final String BASE_PATH = "D:/SecuroServ_Storage";

    public FileEntity encryptSingleFile(String jwtToken, MultipartFile file, User user, Path userFolderPath) throws Exception {

        SecretKeySpec rootKey = vaultSessionCache.getRootKey(jwtToken);
        if (rootKey == null)
            throw new RuntimeException("Root key not found in session — please login again.");

        byte[] fileKeyBytes = new byte[32];
        new SecureRandom().nextBytes(fileKeyBytes);
        SecretKeySpec fileKey = new SecretKeySpec(fileKeyBytes, "AES");


        Cipher keyCipher = Cipher.getInstance("AES/GCM/NoPadding");
        byte[] keyIv = new byte[12];
        new SecureRandom().nextBytes(keyIv);
        keyCipher.init(Cipher.ENCRYPT_MODE, rootKey, new GCMParameterSpec(128, keyIv));
        byte[] encryptedFileKey = keyCipher.doFinal(fileKeyBytes);


        byte[] fileIv = new byte[12];
        new SecureRandom().nextBytes(fileIv);
        Cipher fileCipher = Cipher.getInstance("AES/GCM/NoPadding");
        fileCipher.init(Cipher.ENCRYPT_MODE, fileKey, new GCMParameterSpec(128, fileIv));

        Path encryptedFilePath = userFolderPath.resolve(file.getOriginalFilename() + ".enc");
        Files.createDirectories(encryptedFilePath.getParent());

        try (InputStream in = file.getInputStream();
             FileOutputStream out = new FileOutputStream(encryptedFilePath.toFile());
             CipherOutputStream cos = new CipherOutputStream(out, fileCipher)) {
            in.transferTo(cos);
        }

        FileEntity entity = new FileEntity();
        entity.setUser(user);
        entity.setFileName(file.getOriginalFilename() + ".enc");
        entity.setLocalFilePath(encryptedFilePath.toString());
        entity.setFileSizeBytes(Files.size(encryptedFilePath));
        entity.setEncryptedFileKeyData(Base64.getEncoder().encodeToString(encryptedFileKey));
        entity.setEncryptedFileKeyIv(Base64.getEncoder().encodeToString(keyIv));
        entity.setIv(Base64.getEncoder().encodeToString(fileIv));

        return fileRepository.save(entity);
    }


    public void encryptAndStoreFolderRecursively(
            String jwtToken,
            Path sourceFolder,
            Folder parentFolder,
            User user
    ) throws Exception {

        SecretKeySpec rootKey = vaultSessionCache.getRootKey(jwtToken);
        if (rootKey == null)
            throw new RuntimeException("Root key not found in session — please log in again.");

        Path userVaultBase = Paths.get(BASE_PATH, "user_" + user.getUserId());
        Files.createDirectories(userVaultBase);


        Path encryptedFolderPath = (parentFolder == null)
                ? userVaultBase.resolve(sourceFolder.getFileName().toString())
                : Paths.get(parentFolder.getLocalFolderPath(), sourceFolder.getFileName().toString());
        Files.createDirectories(encryptedFolderPath);

        System.out.println("🧭 Scanning folder: " + sourceFolder);
        if (!Files.exists(sourceFolder)) {
            throw new RuntimeException("Source folder not found: " + sourceFolder);
        }
        System.out.println("Exists: " + Files.exists(sourceFolder));
        System.out.println("Is Directory: " + Files.isDirectory(sourceFolder));
        Files.list(sourceFolder).forEach(p -> System.out.println("   → Found: " + p + " | Dir: " + Files.isDirectory(p)));

        byte[] folderKeyBytes = new byte[32];
        new SecureRandom().nextBytes(folderKeyBytes);
        SecretKeySpec folderKey = new SecretKeySpec(folderKeyBytes, "AES");


        Cipher keyCipher = Cipher.getInstance(ALGO);
        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);
        keyCipher.init(
                Cipher.ENCRYPT_MODE,
                parentFolder == null ? rootKey : decryptFolderKey(parentFolder, rootKey),
                new GCMParameterSpec(128, iv)
        );
        byte[] encryptedFolderKey = keyCipher.doFinal(folderKeyBytes);
        byte[] tag = Arrays.copyOfRange(encryptedFolderKey, encryptedFolderKey.length - 16, encryptedFolderKey.length);


        Folder folder = new Folder();
        folder.setName(sourceFolder.getFileName().toString());
        folder.setUser(user);
        folder.setParentFolder(parentFolder);
        folder.setEncryptedFolderKeyData(Base64.getEncoder().encodeToString(encryptedFolderKey));
        folder.setEncryptedFolderKeyIv(Base64.getEncoder().encodeToString(iv));
        folder.setEncryptedFolderKeyTag(Base64.getEncoder().encodeToString(tag));
        folder.setLocalFolderPath(encryptedFolderPath.toString());

        folder = folderRepository.saveAndFlush(folder);

        System.out.println("🟡 Folder saved: " + folder.getName() +
                " | Parent: " + (folder.getParentFolder() != null) +
                " | Path: " + folder.getLocalFolderPath());


        try (Stream<Path> paths = Files.walk(sourceFolder, 1)) { // depth=1 → only current dir
            Folder finalFolder = folder;
            paths.forEach(filePath -> {
                System.err.println("something is printing here");
                try {
                    if (Files.isDirectory(filePath) && !filePath.equals(sourceFolder)) {
                        System.err.println("📁 Entering subfolder: " + filePath);
                        encryptAndStoreFolderRecursively(jwtToken, filePath, finalFolder, user);
                    } else if (Files.isRegularFile(filePath)) {
                        System.err.println("🧩 Encrypting file: " + filePath.getFileName());
                        encryptAndStoreFile(filePath, finalFolder, folderKey);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Failed to encrypt: " + filePath, e);
                }
            });
        }
    }

    private void encryptAndStoreFile(Path inputFile, Folder folder, SecretKeySpec folderKey) throws Exception {
        System.out.println("❤️ Encrypting file: " + inputFile.getFileName());

        byte[] fileKeyBytes = new byte[32];
        new SecureRandom().nextBytes(fileKeyBytes);
        SecretKeySpec fileKey = new SecretKeySpec(fileKeyBytes, "AES");

        Cipher keyCipher = Cipher.getInstance(ALGO);
        byte[] keyIv = new byte[12];
        new SecureRandom().nextBytes(keyIv);
        keyCipher.init(Cipher.ENCRYPT_MODE, folderKey, new GCMParameterSpec(128, keyIv));
        byte[] encryptedFileKey = keyCipher.doFinal(fileKeyBytes);
        byte[] keyTag = Arrays.copyOfRange(encryptedFileKey, encryptedFileKey.length - 16, encryptedFileKey.length);

        byte[] fileIv = new byte[12];
        new SecureRandom().nextBytes(fileIv);
        Cipher fileCipher = Cipher.getInstance(ALGO);
        fileCipher.init(Cipher.ENCRYPT_MODE, fileKey, new GCMParameterSpec(128, fileIv));

        Path encryptedFilePath = Paths.get(folder.getLocalFolderPath(), inputFile.getFileName().toString() + ".enc");
        Files.createDirectories(encryptedFilePath.getParent());

        try (FileInputStream fis = new FileInputStream(inputFile.toFile());
             FileOutputStream fos = new FileOutputStream(encryptedFilePath.toFile());
             CipherOutputStream cos = new CipherOutputStream(fos, fileCipher)) {
            fis.transferTo(cos);
        }


        if (!entityManager.contains(folder)) {
            folder = folderRepository.findById(folder.getFolderId()).orElseThrow();
        }

        FileEntity fileEntity = new FileEntity();
        fileEntity.setFolder(folder);
        fileEntity.setFileName(inputFile.getFileName().toString() + ".enc");
        fileEntity.setEncryptedFileKeyData(Base64.getEncoder().encodeToString(encryptedFileKey));
        fileEntity.setEncryptedFileKeyIv(Base64.getEncoder().encodeToString(keyIv));
        fileEntity.setEncryptedFileKeyTag(Base64.getEncoder().encodeToString(keyTag));
        fileEntity.setIv(Base64.getEncoder().encodeToString(fileIv));
        fileEntity.setLocalFilePath(encryptedFilePath.toString());
        fileEntity.setFileSizeBytes(Files.size(encryptedFilePath));

        folder.getFiles().add(fileEntity);

        fileRepository.save(fileEntity);
        System.out.println("✅ File encrypted and saved: " + inputFile.getFileName());
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

}
