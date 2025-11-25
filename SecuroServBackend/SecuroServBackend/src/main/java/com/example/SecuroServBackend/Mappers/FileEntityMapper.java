package com.example.SecuroServBackend.Mappers;

import com.example.SecuroServBackend.DTOs.FileEntityDTO;
import com.example.SecuroServBackend.Entity.FileEntity;

public class FileEntityMapper {
    public static FileEntity toEntity(FileEntityDTO dto) {
        if(dto == null) return null;

        FileEntity file = new FileEntity();
        file.setFileId(dto.getFileId());




        return file;
    }

    public static FileEntityDTO toDTO(FileEntity file) {
        if(file == null) return null;
        FileEntityDTO dto = new FileEntityDTO();


        return dto;
    }
}
