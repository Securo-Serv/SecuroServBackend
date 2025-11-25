package com.example.SecuroServBackend.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogsDTO {
    private UUID auditId;
    private UUID userId;
    private String actionType;
    private String resourceType;
    private UUID resourceId;
}
