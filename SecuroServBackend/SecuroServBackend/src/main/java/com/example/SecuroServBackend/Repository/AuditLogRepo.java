package com.example.SecuroServBackend.Repository;

import com.example.SecuroServBackend.Entity.AuditLogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface AuditLogRepo extends JpaRepository<AuditLogs, UUID> {

}
