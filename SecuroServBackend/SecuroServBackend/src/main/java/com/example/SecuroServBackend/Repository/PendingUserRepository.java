package com.example.SecuroServBackend.Repository;

import com.example.SecuroServBackend.Entity.PendingUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PendingUserRepository extends JpaRepository<PendingUser,Long> {
    Optional<PendingUser> findByEmail(String email);
    void deleteByEmail(String email);
}
