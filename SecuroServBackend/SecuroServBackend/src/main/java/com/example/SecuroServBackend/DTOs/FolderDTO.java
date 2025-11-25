package com.example.SecuroServBackend.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FolderDTO {
    private UUID folderId;
    private String name;
    private LocalDateTime creationAT;
    private String type;
}
