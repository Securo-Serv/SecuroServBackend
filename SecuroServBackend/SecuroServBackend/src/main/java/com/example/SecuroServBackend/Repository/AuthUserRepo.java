package com.example.SecuroServBackend.Repository;

import com.example.SecuroServBackend.Entity.AuthUser;
import org.aspectj.apache.bcel.classfile.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthUserRepo extends JpaRepository<AuthUser, UUID> {
  Optional<AuthUser> findByEmail(String email);
}
