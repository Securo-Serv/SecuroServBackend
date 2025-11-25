package com.example.SecuroServBackend.Repository;

import com.example.SecuroServBackend.Entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
@Repository
public interface FolderRepo extends JpaRepository<Folder, UUID> {
    @Query("SELECT f FROM Folder f LEFT JOIN FETCH f.files LEFT JOIN FETCH f.subFolders WHERE f.folderId = :folderId")
    Optional<Folder> findByIdWithFiles(@Param("folderId") UUID folderId);
    @Query("SELECT f FROM Folder f " +
            "LEFT JOIN FETCH f.files " +
            "LEFT JOIN FETCH f.subFolders " +
            "WHERE f.folderId = :id")
    Folder findWithAllData(@Param("id") UUID id);
}
