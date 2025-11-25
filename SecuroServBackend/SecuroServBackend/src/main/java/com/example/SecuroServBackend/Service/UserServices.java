package com.example.SecuroServBackend.Service;

import com.example.SecuroServBackend.DTOs.FileEntityDTO;
import com.example.SecuroServBackend.DTOs.FolderDTO;
import com.example.SecuroServBackend.DTOs.UserDTO;
import com.example.SecuroServBackend.Entity.User;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServices {
    public UserDTO getUserVault(User user) {


        List<FolderDTO> folderDTOs = user.getFolders().stream()
                .filter(folder -> folder.getParentFolder() == null)
                .map(folder -> {
                    FolderDTO dto = new FolderDTO();
                    dto.setFolderId(folder.getFolderId());
                    dto.setName(folder.getName());
                    dto.setType("Folder");
                    dto.setCreationAT(folder.getCreatedAt());
                    return dto;
                })
                .collect(Collectors.toList());


        List<FileEntityDTO> fileDTOs = user.getRootFiles().stream()
                .filter(file -> file.getFolder() == null)
                .map(file -> {
                    FileEntityDTO dto = new FileEntityDTO();
                    dto.setFileId(file.getFileId());
                    dto.setName(file.getFileName());
                    dto.setType("File");
                    dto.setCreationAT(file.getUploadedAt());
                    return dto;
                })
                .collect(Collectors.toList());


        UserDTO vaultDTO = new UserDTO();
        vaultDTO.setUserId(user.getUserId());
        vaultDTO.setEmail(user.getAuthUser().getEmail());
        vaultDTO.setFolders(folderDTOs);
        vaultDTO.setFiles(fileDTOs);

        System.out.println(vaultDTO.toString());

        return vaultDTO;
    }
}
