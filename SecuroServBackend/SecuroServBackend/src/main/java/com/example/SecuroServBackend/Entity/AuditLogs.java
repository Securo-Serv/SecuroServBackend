package com.example.SecuroServBackend.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Entity
@Data
public class AuditLogs {
    @Id
    @GeneratedValue
    @org.hibernate.annotations.UuidGenerator
    private UUID auditId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String actionType;

    @Column(nullable = true)
    private String targetName;

    @Column(nullable = true)
    private String targetType;

    @Column(nullable = true)
    private String details;

    @Column(nullable = false)
    private Instant timestamp = Instant.now();

    @Column(nullable = true)
    private String ipAddress;

}
