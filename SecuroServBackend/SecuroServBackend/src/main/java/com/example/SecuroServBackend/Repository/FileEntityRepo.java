package com.example.SecuroServBackend.Repository;

import com.example.SecuroServBackend.Entity.FileEntity;
import com.example.SecuroServBackend.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FileEntityRepo extends JpaRepository<FileEntity, UUID> {


    List<FileEntity> findAllByUser(User user);

    @Query("SELECT COALESCE(SUM(f.fileSizeBytes), 0) FROM FileEntity f WHERE f.user.userId = :userId")
    long getTotalStorageUsedByUser(@Param("userId") UUID userId);
}
