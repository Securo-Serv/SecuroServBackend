package com.example.SecuroServBackend.Mappers;

import com.example.SecuroServBackend.DTOs.AuditLogsDTO;
import com.example.SecuroServBackend.Entity.AuditLogs;
import org.springframework.stereotype.Component;

public class AuditLogMappers {
    public static AuditLogs toEntity(AuditLogsDTO dto){

        AuditLogs audit = new AuditLogs();
        audit.setAuditId(dto.getAuditId());


        return audit;
    }

    public static AuditLogsDTO toDTO(AuditLogs audit) {
        if(audit == null) return null;

        AuditLogsDTO dto = new AuditLogsDTO();
        dto.setAuditId(audit.getAuditId());


        return dto;
    }
}
