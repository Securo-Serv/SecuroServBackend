package com.example.SecuroServBackend.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileEntityDTO {
    private UUID fileId;
    private UUID folderId;
    private String name;
    private String type;
    private LocalDateTime creationAT;
}
