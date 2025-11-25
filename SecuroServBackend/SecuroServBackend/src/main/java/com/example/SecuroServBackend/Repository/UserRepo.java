package com.example.SecuroServBackend.Repository;

import com.example.SecuroServBackend.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepo extends JpaRepository<User, UUID> {


    Optional<User> findByAuthUser_Email(String email);
}
