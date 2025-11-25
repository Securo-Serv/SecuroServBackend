package com.example.SecuroServBackend.Mappers;

import com.example.SecuroServBackend.DTOs.FolderDTO;
import com.example.SecuroServBackend.Entity.FileEntity;
import com.example.SecuroServBackend.Entity.Folder;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class FolderMapper {
    public static Folder toEntity(FolderDTO dto) {
        if(dto == null) return null;

        Folder folder = new Folder();


        return folder;
    }

    public static FolderDTO toDTO(Folder folder) {
        if(folder == null) return null;

        FolderDTO dto = new FolderDTO();
        dto.setFolderId(folder.getFolderId());


        return dto;
    }
}
